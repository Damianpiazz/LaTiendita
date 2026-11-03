provider "azurerm" {
  features {}

  use_cli = false

  # Custom cloud pointing at floci-az. The provider discovers the cloud over
  # HTTPS at /metadata/endpoints, which is why TLS must be enabled on the
  # emulator (FLOCI_AZ_TLS_ENABLED=true) and its certificate trusted locally.
  environment   = "stack"
  metadata_host = "localhost:4577"

  # Placeholder credentials: floci-az accepts any value without validating it.
  subscription_id = "00000000-0000-0000-0000-000000000001"
  tenant_id       = "00000000-0000-0000-0000-000000000002"
  client_id       = "00000000-0000-0000-0000-000000000003"
  client_secret   = "fake-secret"
}
