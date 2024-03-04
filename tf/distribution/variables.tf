variable "app_name" {
  description = "Application identifier - no spaces"
  type = string
}

variable "domain_name" {
  description = "Domain name to serve the site over"
  type = string
}

variable "hosted_zone" {
  description = "Hosted zone the domain name belongs to"
  type = string
}

variable "immutable_path" {
  description = "A path pattern that can be cached for a long time"
  type = string
}

variable "bucket_regional_domain_name" {
  description = "aws_s3_bucket.your_bucket.bucket_regional_domain_name of bucket to serve"
  type = string
}

variable "cloudfront_access_identity_path" {
  description = "aws_cloudfront_origin_access_identity.identity.cloudfront_access_identity_path of access identity created for bucket"
  type = string
}

variable "certificate_arn" {
  description = "aws_acm_certificate.cert.arn of certificate to use with distribution"
  type = string
}
