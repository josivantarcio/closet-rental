resource "aws_s3_bucket" "images" {
  bucket = "${var.project}-${var.environment}-images"
  tags   = { Name = "${var.project}-${var.environment}-images" }
}

resource "aws_s3_bucket_versioning" "images" {
  bucket = aws_s3_bucket.images.id
  versioning_configuration { status = "Enabled" }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "images" {
  bucket = aws_s3_bucket.images.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Bloqueia acesso público direto ao S3 — acesso apenas via CloudFront
resource "aws_s3_bucket_public_access_block" "images" {
  bucket                  = aws_s3_bucket.images.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# CloudFront Origin Access Control
resource "aws_cloudfront_origin_access_control" "images" {
  name                              = "${var.project}-${var.environment}-oac"
  description                       = "OAC para imagens do Closet Rental"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# CloudFront Distribution
resource "aws_cloudfront_distribution" "images" {
  enabled             = true
  comment             = "${var.project}-${var.environment} images CDN"
  default_root_object = ""
  price_class         = "PriceClass_100" # América do Norte + Europa (mais barato)

  origin {
    domain_name              = aws_s3_bucket.images.bucket_regional_domain_name
    origin_id                = "s3-${aws_s3_bucket.images.id}"
    origin_access_control_id = aws_cloudfront_origin_access_control.images.id
  }

  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "s3-${aws_s3_bucket.images.id}"
    viewer_protocol_policy = "redirect-to-https"
    compress               = true

    forwarded_values {
      query_string = false
      cookies { forward = "none" }
    }

    min_ttl     = 0
    default_ttl = 86400   # 24h
    max_ttl     = 2592000 # 30 dias
  }

  restrictions {
    geo_restriction { restriction_type = "none" }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  tags = { Name = "${var.project}-${var.environment}-cf" }
}

# Bucket policy: permite CloudFront acessar via OAC
data "aws_iam_policy_document" "s3_cf" {
  statement {
    sid    = "AllowCloudFrontAccess"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.images.arn}/*"]
    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values   = [aws_cloudfront_distribution.images.arn]
    }
  }
}

resource "aws_s3_bucket_policy" "images" {
  bucket = aws_s3_bucket.images.id
  policy = data.aws_iam_policy_document.s3_cf.json
}
