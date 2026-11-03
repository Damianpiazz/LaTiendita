resource "azurerm_resource_group" "this" {
  name     = "latiendita"
  location = var.location
}

# ── Container Apps managed environment ────────────────────
resource "azurerm_container_app_environment" "this" {
  name                = "latiendita-env"
  location            = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
}

# ── Orders microservice ───────────────────────────────────
resource "azurerm_container_app" "pedidos" {
  name                         = "pedidos-api"
  resource_group_name          = azurerm_resource_group.this.name
  location                     = azurerm_resource_group.this.location
  container_app_environment_id = azurerm_container_app_environment.this.id
  revision_mode                = "Single"

  template {
    container {
      name   = "pedidos"
      image  = var.image_pedidos
      cpu    = 0.5
      memory = "1Gi"

      ports {
        name           = "http"
        container_port = 8080
      }

      env {
        name  = "AZURE_STORAGE_CONNECTION_STRING"
        value = "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://floci-az:4577/devstoreaccount1;QueueEndpoint=http://floci-az:4577/devstoreaccount1-queue;TableEndpoint=http://floci-az:4577/devstoreaccount1-table;"
      }
    }

    min_replicas = 1
    max_replicas = 2
  }

  ingress {
    external_enabled = true
    target_port      = 8080
    transport        = "http"

    traffic_weight {
      latest_revision = true
      percentage      = 100
    }
  }
}
