output "resource_group" {
  value = azurerm_resource_group.this.name
}

output "pedidos_fqdn" {
  description = "FQDN assigned by floci-az to the container app."
  value       = azurerm_container_app.pedidos.ingress[0].fqdn
}

output "invoke" {
  description = "How to call the service through the emulator."
  value       = "curl -H \"Host: ${azurerm_container_app.pedidos.ingress[0].fqdn}\" http://localhost:4577/"
}
