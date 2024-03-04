terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      configuration_aliases = [
        aws.us-east-1,
      ]
    }
  }
}

data "aws_iam_policy_document" "lambda" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type = "Service"
      identifiers = [
        "lambda.amazonaws.com",
        "edgelambda.amazonaws.com"
      ]
    }
  }
}

resource "aws_iam_role" "lambda_role" {
  name_prefix = "${var.domain_name}"
  assume_role_policy = "${data.aws_iam_policy_document.lambda.json}"
}

resource "aws_iam_role_policy_attachment" "lambda_exec" {
  role = "${aws_iam_role.lambda_role.name}"
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

data "archive_file" "headers" {
  type = "zip"
  output_path = "${path.module}/.zip/headers.zip"

  source {
    filename = "lambda.js"
    content = file("${path.module}/headers-lambda.js")
  }
}

resource "aws_lambda_function" "headers" {
  provider = aws.us-east-1
  function_name = "${var.app_name}-headers"
  filename = data.archive_file.headers.output_path
  source_code_hash = data.archive_file.headers.output_base64sha256
  role = aws_iam_role.lambda_role.arn
  runtime = "nodejs18.x"
  handler = "lambda.handler"
  memory_size = 128
  timeout = 3
  publish = true
}

data "archive_file" "url_rewrite" {
  type = "zip"
  output_path = "${path.module}/.zip/rewrite.zip"

  source {
    filename = "lambda.js"
    content = file("${path.module}/rewrite-lambda.js")
  }
}

resource "aws_lambda_function" "url_rewrite" {
  provider = aws.us-east-1
  function_name = "${var.app_name}-url-rewrite"
  filename = data.archive_file.url_rewrite.output_path
  source_code_hash = data.archive_file.url_rewrite.output_base64sha256
  role = aws_iam_role.lambda_role.arn
  runtime = "nodejs18.x"
  handler = "lambda.handler"
  memory_size = 128
  timeout = 3
  publish = true
}

locals {
  s3_origin_id = "StaticFilesS3BucketOrigin"
}

resource "aws_cloudfront_distribution" "s3_distribution" {
  origin {
    domain_name = "${var.bucket_regional_domain_name}"
    origin_id = "${local.s3_origin_id}"

    s3_origin_config {
      origin_access_identity = "${var.cloudfront_access_identity_path}"
    }
  }

  enabled = true
  is_ipv6_enabled = true
  comment = "${var.app_name} distribution"
  # default_root_object = "index.html"
  aliases = ["${var.domain_name}"]

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "${local.s3_origin_id}"

    forwarded_values {
      query_string = false

      cookies {
        forward = "none"
      }
    }

    min_ttl = 0
    default_ttl = 3600
    max_ttl = 86400
    compress = true
    viewer_protocol_policy = "redirect-to-https"

    lambda_function_association {
      event_type = "viewer-request"
      lambda_arn = "${aws_lambda_function.url_rewrite.qualified_arn}"
      include_body = false
    }

    lambda_function_association {
      event_type = "viewer-response"
      lambda_arn = "${aws_lambda_function.headers.qualified_arn}"
      include_body = false
    }
  }

  # Cache immutable paths for a long time
  ordered_cache_behavior {
    path_pattern = "${var.immutable_path}"
    allowed_methods = ["GET", "HEAD", "OPTIONS"]
    cached_methods = ["GET", "HEAD", "OPTIONS"]
    target_origin_id = "${local.s3_origin_id}"
    min_ttl = 0
    default_ttl = 86400
    max_ttl = 31536000
    compress = true
    viewer_protocol_policy = "redirect-to-https"

    forwarded_values {
      query_string = false
      headers = ["Origin"]
      cookies {
        forward = "none"
      }
    }
  }

  price_class = "PriceClass_100"

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn = "${var.certificate_arn}"
    minimum_protocol_version = "TLSv1.2_2021"
    ssl_support_method = "sni-only"
  }

  custom_error_response {
    error_code = "404"
    response_code = "404"
    response_page_path = "/404/index.html"
  }
}

data "aws_route53_zone" "zone" {
  name = "${var.hosted_zone}"
}

resource "aws_route53_record" "record" {
  name = "${var.domain_name}"
  zone_id = "${data.aws_route53_zone.zone.zone_id}"
  type = "A"

  alias {
    name = "${aws_cloudfront_distribution.s3_distribution.domain_name}"
    zone_id = "${aws_cloudfront_distribution.s3_distribution.hosted_zone_id}"
    evaluate_target_health = true
  }
}
