variable "bucket_name" {
  description = "Bucket to store files in"
  type = string
}

variable "app_name" {
  description = "Descriptive name"
  type = string
}

variable "domain_name" {
  description = "Domain name"
  type = string
}

variable "hosted_zone" {
  description = "Hosted zone"
  type = string
}
