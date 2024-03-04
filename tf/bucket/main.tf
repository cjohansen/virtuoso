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

resource "aws_s3_bucket" "bucket" {
  bucket = "${var.bucket_name}"
}

resource "aws_s3_bucket_ownership_controls" "ownership" {
  bucket = aws_s3_bucket.bucket.id
  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

resource "aws_s3_bucket_acl" "acl" {
  depends_on = [aws_s3_bucket_ownership_controls.ownership]

  bucket = aws_s3_bucket.bucket.id
  acl = "private"
}

resource "aws_s3_bucket_website_configuration" "website" {
  bucket = aws_s3_bucket.bucket.id

  index_document {
    suffix = "index.html"
  }

  error_document {
    key = "404/index.html"
  }
}

resource "aws_cloudfront_origin_access_identity" "identity" {
  comment = "Origin access identity for ${var.app_name}"
}

data "aws_iam_policy_document" "s3_policy" {
  statement {
    actions = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.bucket.arn}/*"]

    principals {
      type = "AWS"
      identifiers = ["${aws_cloudfront_origin_access_identity.identity.iam_arn}"]
    }
  }

  statement {
    actions = ["s3:ListBucket"]
    resources = ["${aws_s3_bucket.bucket.arn}"]

    principals {
      type = "AWS"
      identifiers = ["${aws_cloudfront_origin_access_identity.identity.iam_arn}"]
    }
  }
}

resource "aws_s3_bucket_policy" "bucket_policy" {
  bucket = "${aws_s3_bucket.bucket.id}"
  policy = "${data.aws_iam_policy_document.s3_policy.json}"
}

resource "aws_acm_certificate" "cert" {
  provider = aws.us-east-1
  domain_name = "${var.domain_name}"
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}

data "aws_route53_zone" "zone" {
  name = "${var.hosted_zone}"
}

resource "aws_route53_record" "cert_validation" {
  name = element(aws_acm_certificate.cert.domain_validation_options[*].resource_record_name, 0)
  type = element(aws_acm_certificate.cert.domain_validation_options[*].resource_record_type, 0)
  zone_id = "${data.aws_route53_zone.zone.id}"
  records = [element(aws_acm_certificate.cert.domain_validation_options[*].resource_record_value, 0)]
  ttl = 60
}

resource "aws_acm_certificate_validation" "cert" {
  provider = aws.us-east-1
  certificate_arn = "${aws_acm_certificate.cert.arn}"
  validation_record_fqdns = ["${aws_route53_record.cert_validation.fqdn}"]
}
