# La-Tiendita

## Azure local con floci-az + Terraform

Los microservicios se despliegan como Azure Container Apps contra el emulador
local [floci-az](https://github.com/floci-io/floci-az) (puerto 4577).

### 1. Levantar el emulador

```powershell
docker compose up -d
docker compose ps   # wait until STATUS: healthy
```

### 2. Confiar el certificado TLS (una vez por máquina)

```powershell
.\scripts\trust-cert.ps1
```

floci-az genera un certificado auto-firmado porque el provider `azurerm`
descubre la nube por HTTPS. Sin este paso Terraform falla con
`x509: certificate signed by unknown authority`.

### 3. Variables de entorno

Copiá `.env.example` a `.env` y completá los valores:

```bash
# ── floci-az ──────────────────────────────────────────────
FLOCI_AZ_URL=http://localhost:4577

# Standard devstoreaccount1 connection string (credentials are not validated in dev mode).
# Point endpoints at `floci-az` (compose network) when your code runs inside a container.
AZURE_STORAGE_CONNECTION_STRING="DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:4577/devstoreaccount1;QueueEndpoint=http://localhost:4577/devstoreaccount1-queue;TableEndpoint=http://localhost:4577/devstoreaccount1-table;"

# ── Terraform ─────────────────────────────────────────────
TF_VAR_location="eastus"
TF_VAR_image_pedidos="latiendita/pedidos-api:local"
```

### 4. Desplegar los microservicios

```powershell
# The image must exist in the local daemon: floci-az starts replicas from it
docker build -t latiendita/pedidos-api:local ./services/pedidos

cd infra
terraform init
terraform apply
```

### 5. Invocar

```powershell
$FQDN = terraform -chdir=infra output -raw pedidos_fqdn
curl -H "Host: $FQDN" http://localhost:4577/
```

The `Host` header is how floci-az routes the request to your app: every app
shares port 4577, so the FQDN identifies the target.

### 6. Consola web

Abrir `http://localhost:4577/_floci/ui`

### Estructura

```
LaTiendita/
├── docker-compose.yml        # floci-az: TLS, Container Apps real mode, hybrid storage
├── .env.example              # connection string + Terraform vars (see above)
├── infra/                    # Terraform (azurerm → floci-az)
│   ├── versions.tf
│   ├── providers.tf          # environment = "stack", metadata_host = localhost:4577
│   ├── variables.tf
│   ├── main.tf               # resource group + managed env + pedidos-api
│   └── outputs.tf
├── scripts/trust-cert.ps1    # install floci-az cert in CurrentUser\Root
└── services/pedidos/         # microservice + its Dockerfile
```
