output "bucket_regional_domain_name" {
  value = "${aws_s3_bucket.bucket.bucket_regional_domain_name}"
}

output "cloudfront_access_identity_path" {
  value = "${aws_cloudfront_origin_access_identity.identity.cloudfront_access_identity_path}"
}

output "certificate_arn" {
  value = "${aws_acm_certificate.cert.arn}"
}

output "s3_bucket_arn" {
  value = "${aws_s3_bucket.bucket.arn}"
}
