output "bucket_name"    { value = aws_s3_bucket.images.id }
output "bucket_arn"     { value = aws_s3_bucket.images.arn }
output "cloudfront_url" { value = "https://${aws_cloudfront_distribution.images.domain_name}" }
