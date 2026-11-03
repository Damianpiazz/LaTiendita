variable "location" {
  description = "Azure location. floci-az accepts any value."
  type        = string
  default     = "eastus"
}

variable "image_pedidos" {
  description = "Local Docker image for the orders microservice. It must already exist in the local Docker daemon because floci-az starts revision replicas from it."
  type        = string
  default     = "latiendita/pedidos-api:local"
}
