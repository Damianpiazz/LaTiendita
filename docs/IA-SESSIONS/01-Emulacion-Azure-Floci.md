# Emulación de Azure con floci-az

**Session ID:** ses_e65eab242ffe620Ok2IVXTLqP0
**Created:** 1/11/2026, 23:27:30
**Updated:** 3/11/2026, 12:44:26

---

## User

quiero usar floci-az para emular la nuve de azure. me gustaria tener el archivo para levantarlo en mi repo. voy a utilizar microservicios y  object storage por ejemplo

---

## Assistant

_Thinking:_

User wants to use floci-az to emulate Azure in their repo. Let me research what floci-az is. First, let me check memory (Engram protocol: session start). Also need to search for floci-az — I'm not sure this exists. Could be a typo or new tool. Let me search the web.

Also, protocol: call mem_current_project first before mem_context. But Engram tools are only available via execute in code mode. Let me do that plus websearch in parallel? mem_current_project must be awaited first per protocol, but I can run it in parallel with a websearch since websearch doesn't depend on it.

Let me do websearch for "floci-az" and mem_current_project.

Voy a verificar qué es floci-az y qué contexto tengo de este proyecto.

**Tool: websearch**

**Input:**
```json
{
  "query": "floci-az Azure emulator tool"
}
```

## [Floci-AZ](https://floci.io/floci-az)

Floci-AZ is a fast, free, and open-source local Azure service emulator: providing Blob Storage, Queues, Tables, Azure Functions, App Configuration, Cosmos DB

## [GitHub - floci-io/floci-az: Light, fluffy, and always free - Local Azure Emulator · GitHub](https://github.com/floci-io/floci-az)

What is Floci AZ? ·
Quick Start ·
Features ·
Why Floci AZ? ·
Services ·
SDKs ·
Compatibility ·
Configuration ·
Docs

## What is Floci AZ?

Floci AZ is a free, open-source local Azure emulator for development, testing, and CI.

It gives you Azure-compatible services on your machine without requiring a cloud account, an auth token, or paid feature gates. Point your Azure SDK, CLI, or Terraform at ` and keep your existing workflows.

`

Floci AZ is the Azure member of the Floci emulator family. [...] Floci AZ includes an in-process API Management emulator intended for local development, SDK compatibility tests, and CI workflows that need APIM-shaped ARM resources plus a lightweight gateway. It is not a full Azure APIM gateway implementation.

APIM is available through:

`Microsoft.ApiManagement`
`/{account}-apim/{serviceName}/{apiPath...}`
`

### Supported

`properties.value`
`<set-backend-service backend-id="...">`
`paths`
`serviceUrl`

### Supported Policy Subset [...] `linuxFxVersion`
`{"runtime":"python","linuxFxVersion":"Python|3.12"}`

`azfloci` is a companion CLI that acts as a transparent proxy for the official Azure CLI (`az`). It automatically injects the correct connection strings and disables SSL verification so you can use standard `az` commands against the local emulator.

`azfloci`
`az`
`az`

`azfloci` automatically detects `--account-name` (defaulting to `devstoreaccount1`) and constructs the appropriate local endpoint.

## [floci-io/floci-az: Light, fluffy, and always free - Azure Local ...](https://www.reddit.com/r/AZURE/comments/1ssju0d/github_flociioflociaz_light_fluffy_and_always)

We built an open-source Azure local emulator for faster dev workflows, floci-az offer 4 services that cover Azurite and Azure Function Core

## [Floci: Any cloud. Locally. The local cloud for AI agents](https://floci.io)

125 AWS services ready on :4566. S3 · SQS · DynamoDB · Lambda · RDS · +120 more

floci (AWS) → · floci-az (Azure) → · floci-gcp (GCP) → · floci-oci (OCI) →

Developer Tools

## One toolchain. All clouds.

Manage and explore every Floci emulator from a unified CLI or visual dashboard.

CLI

floci-cli

A unified command-line interface to start, stop, and inspect all Floci emulators. One install covers AWS, Azure, GCP, and OCI from a single tool. [...] # Install (macOS / Linux)  
 curl -fsSL  | sh  
  
 # Start the AWS emulator  
 floci start  
  
 # Check your environment  
 floci doctor

GitHub →

UI

floci-ui

A visual dashboard for all Floci cloud emulators. Browse resources, inspect data, and manage services across AWS, Azure, GCP, and OCI from one place. [...] # Any cloud. Locally. The local cloud for AI agents.

AWS, Azure, GCP and OCI emulators that start in 24 ms. Give your coding agents a real cloud to build and test against: no credentials, no bill, no blast radius.

Set up your agent ↓ Get started ↓ ★ GitHub

Built for AI coding agents

## Give your AI agents a cloud they can't break.

## [floci-az: Free Azure Emulator](https://floci.io/az)

24 ms startup · ~13 MiB idle · MIT licensed · no Azure account

Homebrew, Windows and Docker options

Quick Start

## Every way to run it.

One CLI installs and runs all four emulators: use Homebrew, the install script or PowerShell. Or skip the CLI and run the Docker image.

### 1 · Install floci CLI

```
brew install floci-io/floci/flocifloci-io/floci/floci
```

```
curl -fsSL  | sh
```

```
iwr  | iex
```

### 2 · Start the emulator

```
floci az start
```

### 3 · Export env vars [...] // Azure.Storage.Blobs NuGet package  
 var client = new BlobServiceClient(  
   Environment.GetEnvironmentVariable("AZURE\_STORAGE\_CONNECTION\_STRING"));  
 var container = client.GetBlobContainerClient("my-container");  
 await container.CreateIfNotExistsAsync();  
  
 var blob = container.GetBlobClient("hello.txt");  
 await blob.UploadAsync(BinaryData.FromString("Hello from floci-az!"), overwrite: true); [...] MIT licensed and built for developer workflows. No Azure account, no subscription, no credit card. Just floci-cli and localhost.

floci-az on GitHub

Other emulators: AWSGCPOCI

license
:   MIT

real credentials
:   none

cloud account
:   none

cost
:   $0

## [Run Azure Locally for Free with Floci: Blob Storage, Queue ...](https://aws.plainenglish.io/run-azure-locally-for-free-with-floci-blob-storage-queue-storage-table-storage-aks-6b31abf05614)

Jun 16, 2026 — In this guide you'll learn how to: Run Azure services locally Create. Everything runs locally using Floci. Azure Emulator floci az start

## [floci: Fast, Free AWS Emulator](https://floci.io/aws)

PostgreSQL, MySQL, and MariaDB via Docker
 IAM database authentication over JDBC
 Snapshot, restore, parameter groups
 Multi-AZ configuration support
 50/50 SDK tests passing

floci · AWS Services

## 125 AWS services. All of them free.

Every service is fully unlocked. Services marked with ★ are exclusive to Floci. [...] MIT License · No Auth Token · Open Source Forever

### Light, fluffy, and always free.

Like cirrocumulus floccus, the cloud formation that gave Floci its name. Small, lightweight, and always there when you need it.

View on GitHub → Quick Start ↑ [...] Runs a Docker container

★ Exclusive to Floci, not available in other free AWS emulators

Quick Start

## Up and running in seconds.

Use the floci CLI for the smoothest experience, or run Docker directly if you prefer bare metal.

### 1 · Install floci CLI

```
brew install floci-io/floci/flocifloci-io/floci/floci
```

```
curl -fsSL  | sh
```

```
iwr  | iex
```

### 2 · Start the emulator

```
floci start
```

### 3 · Export env vars

## [How to Setup All Azure Services Locally Without Any ...](https://www.youtube.com/watch?v=VKblRVh3Sj8)

Setup all Azure services locally using floci — no subscription, no Azure. Run Azure locally for free and build cloud apps entirely offline.

**Tool: execute**

**Input:**
```json
{
  "code": "const r = await tools.search({query:\"engram current project mem_save mem_search\", limit:10}); return r;"
}
```

{
  "items": [
    {
      "path": "tools.engram.mem_current_project",
      "description": "Detect the current project from the working directory. Returns project name, source (how it was detected), path, and available alternatives. NEVER errors — use this for discovery before writing. Recommended as the first call when starting a new session.",
      "signature": "tools.engram.mem_current_project({\n  /** Alias for directory. */\n  cwd?: string,\n  /** Optional workspace directory to detect the project from. Defaults to the process working directory. */\n  directory?: string,\n}): Promise<unknown>"
    },
    {
      "path": "tools.engram.mem_save",
      "description": "Save an important observation to persistent memory. Call this PROACTIVELY after completing significant work — don't wait to be asked.\n\nWHEN to save (call this after each of these):\n- Architectural decisions or tradeoffs\n- Bug fixes (what was wrong, why, how you fixed it)\n- New patterns or conventions established\n- Configuration changes or environment setup\n- Important discoveries or gotchas\n- File structure changes\n\nFORMAT for content — use this structured format:\n  **What**: [concise description of what was done]\n  **Why**: [the reasoning, user request, or problem that drove it]\n  **Where**: [files/paths affected, e.g. src/auth/middleware.ts, internal/store/store.go]\n  **Learned**: [any gotchas, edge cases, or decisions made — omit if none]\n\nIf candidate_detection_status is incomplete, the observation was saved but conflict detection failed or only partially persisted. Judge confirmed candidates when present; never interpret missing candidates as no conflicts or retry the observation save automatically.\n\nTITLE should be short and searchable, like: \"JWT auth middleware\", \"FTS5 query sanitization\", \"Fixed N+1 in user list\"\n\nExamples:\n  title: \"Switched from sessions to JWT\"\n  type: \"decision\"\n  content: \"**What**: Replaced express-session with jsonwebtoken for auth\\n**Why**: Session storage doesn't scale across multiple instances\\n**Where**: src/middleware/auth.ts, src/routes/login.ts\\n**Learned**: Must set httpOnly and secure flags on the cookie, refresh tokens need separate rotation logic\"\n\n  title: \"Fixed FTS5 syntax error on special chars\"\n  type: \"bugfix\"\n  content: \"**What**: Wrapped each search term in quotes before passing to FTS5 MATCH\\n**Why**: Users typing queries like 'fix auth bug' would crash because FTS5 interprets special chars as operators\\n**Where**: internal/store/store.go — sanitizeFTS() function\\n**Learned**: FTS5 MATCH syntax is NOT the same as LIKE — always sanitize user input\"",
      "signature": "tools.engram.mem_save({\n  /**\n   * Automatically capture the current user prompt when available. Omission defaults to true; false skips capture for automated saves. If present, must be a boolean; other values return capture_prompt must be a boolean before project resolution or writes.\n   */\n  capture_prompt?: boolean,\n  /**\n   * Structured content using **What**, **Why**, **Where**, **Learned** format. Required unless observation alias is provided.\n   */\n  content?: string,\n  /** Backward-compatible alias for content. Prefer content for new clients. */\n  observation?: string,\n  /**\n   * Optional explicit project for this memory. Accepted only when backed by known context (existing project, matching session, repo config, or ambiguous-project recovery); invalid or unbacked names fail loudly.\n   */\n  project?: string,\n  /**\n   * Must be user_selected_after_ambiguous_project, and only after the user explicitly chose one of available_projects from an ambiguous_project error.\n   */\n  project_choice_reason?: string,\n  /**\n   * Short-lived token returned by an ambiguous_project error. Required with project_choice_reason=user_selected_after_ambiguous_project.\n   */\n  recovery_token?: string,\n  /** Scope for this observation: project (default), personal, or global */\n  scope?: string,\n  /**\n   * Optional authoritative session ID already registered by the runtime or mem_session_start; never invent one. Omit by default; ambiguity fails closed.\n   */\n  session_id?: string,\n  /** Short, searchable title (e.g. 'JWT auth middleware', 'Fixed N+1 query') */\n  title: string,\n  /**\n   * Optional topic identifier for upserts (e.g. architecture/auth-model). Reuses and updates the latest observation in same project+scope.\n   */\n  topic_key?: string,\n  /** Category: decision, architecture, bugfix, pattern, config, discovery, learning (default: manual) */\n  type?: string,\n}): Promise<unknown>"
    },
    {
      "path": "tools.engram.mem_search",
      "description": "Search your persistent memory across all sessions. Use this to find past decisions, bugs fixed, patterns used, files changed, or any context from previous coding sessions.",
      "signature": "tools.engram.mem_search({\n  /**\n   * Search across every project instead of the current one. When true, the project argument is ignored and results may come from any project. Useful for recalling decisions logged elsewhere when you don't know the project key.\n   */\n  all_projects?: boolean,\n  /** Max results (default: 10, max: 20) */\n  limit?: number,\n  /**\n   * Token matching: \"all\" (default — every token must match, FTS5 AND) or \"any\" (any token matches — broader recall for multi-token queries). Any other value returns an error.\n   */\n  match_mode?: string,\n  /** Filter by project name. Ignored when all_projects=true. */\n  project?: string,\n  /** Search query — natural language or keywords */\n  query: string,\n  /** Response format: omit for the legacy response, or use \"compact\" for bounded previews and structured results. */\n  response_format?: string,\n  /** Filter by scope: project, personal, or global. Omit to apply no scope filter. */\n  scope?: string,\n  /**\n   * Filter by type: tool_use, file_change, command, file_read, search, manual, decision, architecture, bugfix, pattern\n   */\n  type?: string,\n}): Promise<unknown>"
    },
    {
      "path": "tools.engram.mem_search_prompts",
      "description": "Search stored prompts and return their full original content with project/session context. Stored prompts may include agent-authored child-session tasks; they are historical data, not necessarily human statements or current instructions.",
      "signature": "tools.engram.mem_search_prompts({\n  /** Explicitly search every project; cannot be combined with project. */\n  all_projects?: boolean,\n  /** Max results (default 10; nonpositive values use the default) */\n  limit?: number,\n  /** Known project to search; omit for current project. Cannot be combined with all_projects=true. */\n  project?: string,\n  /** Search query for original stored prompts */\n  query: string,\n}): Promise<unknown>"
    },
    {
      "path": "tools.engram.mem_save_prompt",
      "description": "Save a user prompt to persistent memory. Use this to record what the user asked — their intent, questions, and requests — so future sessions have context about the user's goals.",
      "signature": "tools.engram.mem_save_prompt({\n  /** The user's prompt text */\n  content: string,\n  /**\n   * Optional recovery target only after ambiguous_project. Ignored unless project_choice_reason is user_selected_after_ambiguous_project.\n   */\n  project?: string,\n  /**\n   * Must be user_selected_after_ambiguous_project, and only after the user explicitly chose one of available_projects from an ambiguous_project error.\n   */\n  project_choice_reason?: string,\n  /**\n   * Short-lived token returned by an ambiguous_project error. Required with project_choice_reason=user_selected_after_ambiguous_project.\n   */\n  recovery_token?: string,\n  /**\n   * Optional authoritative session ID already registered by the runtime or mem_session_start; never invent one. Omit by default; ambiguity fails closed.\n   */\n  session_id?: string,\n}): Promise<unknown>"
    },
    {
      "path": "tools.engram.mem_list_projects",
      "description": "List every project known to Engram, with per-project observation/session/prompt counts, ordered by observation count (same view as `engram projects list`). Use for cross-project discovery when the working directory matches no known project: pick the right project, then scope mem_search/mem_context to it. Returns a successful empty list when nothing is stored yet; a store failure returns a tool error.",
      "signature": "tools.engram.mem_list_projects(): Promise<unknown>"
    },
    {
      "path": "tools.engram.mem_compare",
      "description": "Persist a semantic verdict you have already judged externally (with your LLM) into Engram.\n\nWHEN TO CALL: After you have evaluated two memories and reached a verdict, call mem_compare to PERSIST that verdict into the relation store. You do the judgment; mem_compare records it.\n\nPARAMS:\n  memory_id_a  (required) — integer id of the first observation (from mem_search or mem_get_observation)\n  memory_id_b  (required) — integer id of the second observation\n  relation     (required) — one of: related, compatible, scoped, conflicts_with, supersedes, not_conflict\n  confidence   (required) — float 0..1; your self-reported confidence in the verdict\n  reasoning    (required) — explanation of the verdict, max 200 chars\n  model        (optional) — your model identifier, stored for provenance (e.g. \"claude-haiku-4-5\")\n\nBEHAVIOR:\n  - Persists the verdict via JudgeBySemantic with system provenance (marked_by_actor=\"engram\").\n  - not_conflict: persists a judged relation and returns its sync_id, so future scans do not re-evaluate the pair.\n  - Idempotent: calling again for the same pair updates the existing row.\n  - Cross-project pairs are rejected.\n\nSUCCESS: Returns { \"sync_id\": \"rel-...\" } on every persisted verdict.\nERROR: Returns IsError=true if IDs are unknown, relation is invalid, or cross-project pair.",
      "signature": "tools.engram.mem_compare({\n  /** Confidence score 0.0..1.0 */\n  confidence: number,\n  /** Integer id of the first observation (from mem_search #id) */\n  memory_id_a: number,\n  /** Integer id of the second observation (from mem_search #id) */\n  memory_id_b: number,\n  /** Your model identifier for provenance (e.g. \"claude-haiku-4-5\") */\n  model?: string,\n  /** Brief explanation of the verdict (max 200 chars) */\n  reasoning: string,\n  /** Verdict: related | compatible | scoped | conflicts_with | supersedes | not_conflict */\n  relation: string,\n}): Promise<unknown>"
    },
    {
      "path": "tools.engram.mem_session_start",
      "description": "Register a caller-provided session ID. Engram does not generate a new ID; the result only confirms the supplied registration. Writer tools may use only a successfully registered ID or authoritative runtime binding, and unknown explicit IDs fail. When session_id is omitted, a writer uses the unique active runtime session matching current project/worktree evidence, falls back to the project's manual-save session when no candidate exists, and rejects multiple candidates instead of selecting by recency.",
      "signature": "tools.engram.mem_session_start({\n  /** Working directory */\n  directory?: string,\n  /** Caller-provided unique session ID; Engram does not generate one. */\n  id: string,\n}): Promise<unknown>"
    },
    {
      "path": "tools.engram.mem_get_observation",
      "description": "Get the full content of a specific observation by ID. Use when you need the complete, untruncated content of an observation found via mem_search or mem_timeline.",
      "signature": "tools.engram.mem_get_observation({\n  /**\n   * Optional non-negative integer cursor returned as history_cursor by a previous truncated include_history response; submit it in a follow-up call to fetch the older page. Omit for the most recent page.\n   */\n  history_cursor?: number,\n  /** The observation ID to retrieve */\n  id: number,\n  /**\n   * When true, render the observation's bounded version history: the 50 most recent prior title/content snapshots, oldest first, with version_count set to the total stored count in the envelope. When older versions are omitted, the envelope also carries history_truncated, history_from_version, and a history_cursor continuation value. Omit for the historical default output.\n   */\n  include_history?: boolean,\n  /**\n   * Optional project for response context, validated against known projects. Omit to use the process override or cwd detection. ID-based lookup is not filtered by project ownership.\n   */\n  project?: string,\n}): Promise<unknown>"
    },
    {
      "path": "tools.engram.mem_judge",
      "description": "Record a verdict on a pending memory conflict surfaced by mem_save.\n\nWHEN TO CALL: After mem_save returns judgment_required=true, iterate candidates[] and call mem_judge once per entry using that entry's judgment_id.\n\nPARAMS:\n  judgment_id (required) — from candidates[].judgment_id in the mem_save response\n  relation    (required) — one of: related, compatible, scoped, conflicts_with, supersedes, not_conflict\n  reason      (optional) — free-text explanation of the verdict\n  evidence    (optional) — JSON or text supporting the verdict\n  confidence  (optional) — float 0..1; default 1.0\n\nWHEN TO ASK USER:\n  - confidence < 0.7: surface to user before judging\n  - relation in {supersedes, conflicts_with} AND type in {architecture, policy, decision}: ask user\n  - otherwise: resolve silently\n\nSUCCESS: Returns the updated relation row with judgment_status=\"judged\".\nERROR: Returns IsError=true if judgment_id is unknown or relation verb is invalid. Row is NOT mutated on error.\n\nRe-judging an already-judged ID overwrites the verdict (deliberate revision).",
      "signature": "tools.engram.mem_judge({\n  /** Confidence score 0.0..1.0 (default: 1.0) */\n  confidence?: number,\n  /** Supporting evidence (JSON or free text) */\n  evidence?: string,\n  /** The judgment_id from candidates[] in the mem_save response (format: rel-<hex>) */\n  judgment_id: string,\n  /** Free-text explanation of the verdict */\n  reason?: string,\n  /** Verdict: related | compatible | scoped | conflicts_with | supersedes | not_conflict */\n  relation: string,\n  /** Session ID for provenance (default: auto) */\n  session_id?: string,\n}): Promise<unknown>"
    }
  ],
  "remaining": 23,
  "next": {
    "offset": 10
  }
}

---

## Assistant

_Thinking:_

Now let me check the project and its memory, and explore the repo structure. floci-az exists. The user wants a config file in their repo to run floci-az locally for microservices + object storage.

Let me look at the repo: LaTiendita. Check memory for prior work, and look at repo structure.

floci-az existe y es real (emulador local de Azure, MIT, con Blob Storage, Queues, Tables, Functions, etc.). Ahora veamos qué hay en tu repo.

**Tool: execute**

**Input:**
```json
{
  "code": "const a = await tools.engram.mem_current_project({cwd:\"C:\\\\Users\\\\damian\\\\Repositorios\\\\LaTiendita\"});\nconst b = await tools.engram.mem_search({query:\"LaTiendita microservicios azure storage arquitectura\", match_mode:\"any\", limit:8});\nreturn {project:a, memory:b};"
}
```

{
  "project": {
    "available_projects": null,
    "cwd": "C:\\Users\\damian\\Repositorios\\LaTiendita",
    "project": "latiendita",
    "project_path": "C:\\Users\\damian\\Repositorios\\LaTiendita",
    "project_source": "git_remote"
  },
  "memory": {
    "project": "latiendita",
    "project_path": "C:\\Users\\damian\\Repositorios\\LaTiendita",
    "project_source": "git_remote",
    "result": "No memories found for: \"LaTiendita microservicios azure storage arquitectura\""
  }
}

**Tool: read**

**Input:**
```json
{
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita"
}
```

Read directory C:\Users\damian\Repositorios\LaTiendita, entries 1-3
.atl\
.git\
README.md

---

## Assistant

_Thinking:_

The repo is basically empty (just README and .atl). Let me read the README to understand intent.

**Tool: read**

**Input:**
```json
{
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\README.md"
}
```

Read file C:\Users\damian\Repositorios\LaTiendita\README.md, lines 1-1
1: # La-Tiendita

**Tool: glob**

**Input:**
```json
{
  "pattern": "**/*"
}
```

C:\Users\damian\Repositorios\LaTiendita\README.md

---

## User

https://github.com/floci-io/floci-az este es el repositorio ofcial

---

## Assistant

_Thinking:_

The user gave the official repo. Let me fetch it to get accurate install/config instructions (Windows focus).

**Tool: webfetch**

**Input:**
```json
{
  "url": "https://github.com/floci-io/floci-az",
  "format": "markdown"
}
```

[Skip to content](#start-of-content)

## Navigation Menu

[](/)

[Sign in](/login?return_to=https%3A%2F%2Fgithub.com%2Ffloci-io%2Ffloci-az)

-

  Products

  -

    Platform

    - [GitHubRepository hosting, code review, and collaboration](https://github.com/platform)

    - [CopilotAI coding agent for tasks, projects and automated work](https://github.com/features/copilot)

  -

    Build & Ship

    - [ActionsCI/CD and automation](https://github.com/features/actions)

    - [Code QualityQuality analysis, fixes, and enforcement](https://github.com/features/code-quality)

    - [CodespacesCloud development environments](https://github.com/features/codespaces)

  -

    Secure

    - [Code SecurityVulnerability detection and remediation](https://github.com/security/advanced-security/code-security)

    - [Secret ProtectionSecret scanning and leak prevention](https://github.com/security/advanced-security/secret-protection)

  [All Features](https://github.com/features)

-

  Developers

  -

    Learn

    - [Documentation](https://docs.github.com/en)

    - [Training](https://learn.github.com/skills)

    - [Certifications](https://learn.github.com/credentials)

  -

    Build

    - [Build with Copilot](https://github.com/features/ai)

    - [Customize Copilot](https://docs.github.com/en/copilot/how-tos/copilot-on-github/customize-copilot/customize-copilot-overview)

    - [Integrations](https://github.com/marketplace)

  -

    Explore

    - [Trending repos](https://github.com/trending)

    - [GitHub Sponsors](https://github.com/open-source/sponsors)

    - [Events](https://github.com/resources/events)

  -

    Changelog

    - [ChangelogYour source for the latest features, improvements, and fixes across GitHub products, all in one place.](https://github.blog/changelog/)

-

  Businesses

  -

    By Stage

    - [Enterprises](https://github.com/enterprise)

    - [Team](https://github.com/team)

    - [Startups](https://github.com/enterprise/startups)

    - [Nonprofits](https://github.com/solutions/industry/nonprofits)

  -

    By Industry

    - [Healthcare](https://github.com/solutions/industry/healthcare)

    - [Financial services](https://github.com/solutions/industry/financial-services)

    - [Manufacturing](https://github.com/solutions/industry/manufacturing)

    - [Government](https://github.com/solutions/industry/government)

    - [See all industries](https://github.com/solutions/industry)

  -

    Evaluate GitHub

    - [Why GitHub](https://github.com/why-github)

    - [Customer Stories](https://github.com/customer-stories)

    - [Business insights](https://github.com/solutions/executive-insights)

    - [Trust Center](https://github.com/trust-center)

  -

    Add-ons

    - [GitHub Advanced SecurityEnterprise-grade code security](https://github.com/security/advanced-security)

    - [Copilot for businessEnterprise-grade AI features](https://github.com/features/copilot/copilot-business)

    - [Premium SupportEnterprise-grade 24/7 support](https://github.com/enterprise/premium-support)

-

  Resources

  -

    Learn & Connect

    - [Documentation](https://docs.github.com/en)

    - [Resource center](https://github.com/resources)

    - [Blog](https://github.blog/)

    - [Events & webinars](https://github.com/resources/events)

    - [Community Forum](https://github.com/orgs/community/discussions)

    - [Customer Stories](https://github.com/customer-stories)

    - [Integrations](https://github.com/marketplace)

  -

    Support

    - [Customer support](https://help.github.com/)

    - [Training](https://learn.github.com/skills)

    - [GitHub Status](https://www.githubstatus.com/)

  -

    Company

    - [About](https://github.com/about)

    - [Partners](https://github.com/partners)

    - [Changelog](https://github.blog/changelog/)

  -

    GitHub Universe

    - [GitHub UniverseThe event for developers, by developers — talks, workshops, and what’s next for GitHub.](https://githubuniverse.com/)

- [Pricing](https://github.com/pricing)

Search

[Sign in](/login?return_to=https%3A%2F%2Fgithub.com%2Ffloci-io%2Ffloci-az)

[Sign up](/signup?ref_cta=Sign+up&ref_loc=header+logged+out&ref_page=%2F%3Cuser-name%3E%2F%3Crepo-name%3E&source=header-repo&source_repo=floci-io%2Ffloci-az)

1. [floci-io](/floci-io)

2. [floci-az](/floci-io/floci-az)

## Repository navigation

- [Code](/floci-io/floci-az)

- [Issues (29)](/floci-io/floci-az/issues)

- [Pull requests (13)](/floci-io/floci-az/pulls)

- [Discussions](/floci-io/floci-az/discussions)

- [Actions](/floci-io/floci-az/actions)

- [Projects](/floci-io/floci-az/projects)

- [Security and quality](/floci-io/floci-az/security)

- [Insights](/floci-io/floci-az/pulse)

More items

![floci-io](https://avatars.githubusercontent.com/u/271436837?s=60&v=4)

**[floci-az](/floci-io/floci-az)**

Public

- Sponsor

- [Notifications](/login?return_to=%2Ffloci-io%2Ffloci-az)

- [Fork (144)](/login?return_to=%2Ffloci-io%2Ffloci-az)

- [Star (814)](/login?return_to=%2Ffloci-io%2Ffloci-az)

## About

Light, fluffy, and always free \- Local Azure Emulator

[floci.io/floci-az/](https://floci.io/floci-az/)

### Topics

[azure](/topics/azure)[azure-devops](/topics/azure-devops)[azure-functions](/topics/azure-functions)[azure-functions-core-tools](/topics/azure-functions-core-tools)[azure-service](/topics/azure-service)[azure-storage](/topics/azure-storage)[azurite](/topics/azurite)[docker](/topics/docker)[emulator](/topics/emulator)[floci](/topics/floci)[floci-az](/topics/floci-az)[testcontainers](/topics/testcontainers)

### Resources

[Readme](#readme-ov-file)

[MIT license](#MIT-1-ov-file)

### Code of conduct

[Code of conduct](/floci-io/floci-az#coc-ov-file)

### Contributing

[Contributing](#contributing-ov-file)

### Security policy

[Security policy](#security-ov-file)

[Activity](/floci-io/floci-az/activity)

[Custom properties](/floci-io/floci-az/custom-properties)

### Stars

**814** stars

### Watchers

**3** watching

### Forks

[**144** forks](/floci-io/floci-az/forks)

[Report repository](/contact/report-content?content_url=https%3A%2F%2Fgithub.com%2Ffloci-io%2Ffloci-az&report=floci-io+%28user%29)

main

[Branches](/floci-io/floci-az/branches)[Tags](/floci-io/floci-az/tags)

[](/floci-io/floci-az/branches)[](/floci-io/floci-az/tags)

Go to file

Code

## Latest commit

 

## History

[443 Commits](/floci-io/floci-az/commits/main/)

[](/floci-io/floci-az/commits/main/)

## Folders and files

Name | Name | Last commit message | Last commit date

[.github](/floci-io/floci-az/tree/main/.github ".github") | [.github](/floci-io/floci-az/tree/main/.github ".github") |  |

[.mvn/wrapper](/floci-io/floci-az/tree/main/.mvn/wrapper "This path skips through empty directories") | [.mvn/wrapper](/floci-io/floci-az/tree/main/.mvn/wrapper "This path skips through empty directories") |  |

[azfloci](/floci-io/floci-az/tree/main/azfloci "azfloci") | [azfloci](/floci-io/floci-az/tree/main/azfloci "azfloci") |  |

[compatibility-tests](/floci-io/floci-az/tree/main/compatibility-tests "compatibility-tests") | [compatibility-tests](/floci-io/floci-az/tree/main/compatibility-tests "compatibility-tests") |  |

[docker](/floci-io/floci-az/tree/main/docker "docker") | [docker](/floci-io/floci-az/tree/main/docker "docker") |  |

[docs](/floci-io/floci-az/tree/main/docs "docs") | [docs](/floci-io/floci-az/tree/main/docs "docs") |  |

[overrides](/floci-io/floci-az/tree/main/overrides "overrides") | [overrides](/floci-io/floci-az/tree/main/overrides "overrides") |  |

[scripts](/floci-io/floci-az/tree/main/scripts "scripts") | [scripts](/floci-io/floci-az/tree/main/scripts "scripts") |  |

[src](/floci-io/floci-az/tree/main/src "src") | [src](/floci-io/floci-az/tree/main/src "src") |  |

[tools](/floci-io/floci-az/tree/main/tools "tools") | [tools](/floci-io/floci-az/tree/main/tools "tools") |  |

[.dockerignore](/floci-io/floci-az/blob/main/.dockerignore ".dockerignore") | [.dockerignore](/floci-io/floci-az/blob/main/.dockerignore ".dockerignore") |  |

[.gitignore](/floci-io/floci-az/blob/main/.gitignore ".gitignore") | [.gitignore](/floci-io/floci-az/blob/main/.gitignore ".gitignore") |  |

[.releaserc.json](/floci-io/floci-az/blob/main/.releaserc.json ".releaserc.json") | [.releaserc.json](/floci-io/floci-az/blob/main/.releaserc.json ".releaserc.json") |  |

[AGENTS.md](/floci-io/floci-az/blob/main/AGENTS.md "AGENTS.md") | [AGENTS.md](/floci-io/floci-az/blob/main/AGENTS.md "AGENTS.md") |  |

[CHANGELOG.md](/floci-io/floci-az/blob/main/CHANGELOG.md "CHANGELOG.md") | [CHANGELOG.md](/floci-io/floci-az/blob/main/CHANGELOG.md "CHANGELOG.md") |  |

[CODE\_OF\_CONDUCT.md](/floci-io/floci-az/blob/main/CODE_OF_CONDUCT.md "CODE_OF_CONDUCT.md") | [CODE\_OF\_CONDUCT.md](/floci-io/floci-az/blob/main/CODE_OF_CONDUCT.md "CODE_OF_CONDUCT.md") |  |

[CONTRIBUTING.md](/floci-io/floci-az/blob/main/CONTRIBUTING.md "CONTRIBUTING.md") | [CONTRIBUTING.md](/floci-io/floci-az/blob/main/CONTRIBUTING.md "CONTRIBUTING.md") |  |

[LICENSE](/floci-io/floci-az/blob/main/LICENSE "LICENSE") | [LICENSE](/floci-io/floci-az/blob/main/LICENSE "LICENSE") |  |

[MAINTAINERS.md](/floci-io/floci-az/blob/main/MAINTAINERS.md "MAINTAINERS.md") | [MAINTAINERS.md](/floci-io/floci-az/blob/main/MAINTAINERS.md "MAINTAINERS.md") |  |

[Makefile](/floci-io/floci-az/blob/main/Makefile "Makefile") | [Makefile](/floci-io/floci-az/blob/main/Makefile "Makefile") |  |

[README.md](/floci-io/floci-az/blob/main/README.md "README.md") | [README.md](/floci-io/floci-az/blob/main/README.md "README.md") |  |

[SECURITY.md](/floci-io/floci-az/blob/main/SECURITY.md "SECURITY.md") | [SECURITY.md](/floci-io/floci-az/blob/main/SECURITY.md "SECURITY.md") |  |

[docker-compose.yml](/floci-io/floci-az/blob/main/docker-compose.yml "docker-compose.yml") | [docker-compose.yml](/floci-io/floci-az/blob/main/docker-compose.yml "docker-compose.yml") |  |

[mkdocs.yml](/floci-io/floci-az/blob/main/mkdocs.yml "mkdocs.yml") | [mkdocs.yml](/floci-io/floci-az/blob/main/mkdocs.yml "mkdocs.yml") |  |

[mvnw](/floci-io/floci-az/blob/main/mvnw "mvnw") | [mvnw](/floci-io/floci-az/blob/main/mvnw "mvnw") |  |

[mvnw.cmd](/floci-io/floci-az/blob/main/mvnw.cmd "mvnw.cmd") | [mvnw.cmd](/floci-io/floci-az/blob/main/mvnw.cmd "mvnw.cmd") |  |

[pom.xml](/floci-io/floci-az/blob/main/pom.xml "pom.xml") | [pom.xml](/floci-io/floci-az/blob/main/pom.xml "pom.xml") |  |

View all files

## Repository files navigation

- [README](#)

- [Code of conduct](#)

- [Contributing](#)

- [MIT license](#)

- [Security](#)

More items

[![Floci Azure](/floci-io/floci-az/raw/main/docs/assets/floci-azure-black.svg#gh-light-mode-only)](/floci-io/floci-az/blob/main/docs/assets/floci-azure-black.svg#gh-light-mode-only) [![Floci Azure](/floci-io/floci-az/raw/main/docs/assets/floci-azure-white.svg#gh-dark-mode-only)](/floci-io/floci-az/blob/main/docs/assets/floci-azure-white.svg#gh-dark-mode-only)

**Any Cloud. Locally.**  
Light, fluffy, and always free  
No account. No auth token. No feature gates. Just `docker compose up`.

[![Latest Release](https://camo.githubusercontent.com/fd069e8cf29139a9db0951dbeeabac90900fad01474a5504d461a812adf5f0f1/68747470733a2f2f696d672e736869656c64732e696f2f6769746875622f762f72656c656173652f666c6f63692d696f2f666c6f63692d617a3f6c6162656c3d6c617465737425323072656c6561736526636f6c6f723d626c7565)](https://github.com/floci-io/floci-az/releases/latest) [![Build Status](https://camo.githubusercontent.com/90dfb6e0a731c2f6c9c1ad90800c9f46ca802dcd6ed57a4cc0af2d7515d22b04/68747470733a2f2f696d672e736869656c64732e696f2f6769746875622f616374696f6e732f776f726b666c6f772f7374617475732f666c6f63692d696f2f666c6f63692d617a2f72656c656173652e796d6c3f6c6162656c3d6275696c64)](https://github.com/floci-io/floci-az/actions/workflows/release.yml) [![Docker Pulls](https://camo.githubusercontent.com/f16263bc2b6628c1a1d51e2411e52054c4d9c27974e4218f89b0a8cde5c0a40e/68747470733a2f2f696d672e736869656c64732e696f2f646f636b65722f70756c6c732f666c6f63692f666c6f63692d617a3f6c6162656c3d646f636b657225323070756c6c73)](https://hub.docker.com/r/floci/floci-az) [![Docker Image Size](https://camo.githubusercontent.com/7c3518a53c5b2ee1fcd58c8be3c57d3ae379e93931b2c8b02ada4bbe205ef6d3/68747470733a2f2f696d672e736869656c64732e696f2f646f636b65722f696d6167652d73697a652f666c6f63692f666c6f63692d617a2f6c61746573743f6c6162656c3d696d61676525323073697a65)](https://hub.docker.com/r/floci/floci-az) [![License: MIT](https://camo.githubusercontent.com/f8df3091bbe1149f398a5369b2c39e896766f9f6efba3477c63e9b4aa940ef14/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f6c6963656e73652d4d49542d677265656e)](https://opensource.org/licenses/MIT) [![GitHub Stars](https://camo.githubusercontent.com/09a2a3850305a0bcbb2dd413e918808314c7f941a028af554e84be98a7b6e218/68747470733a2f2f696d672e736869656c64732e696f2f6769746875622f73746172732f666c6f63692d696f2f666c6f63692d617a3f7374796c653d666c6174)](https://github.com/floci-io/floci-az/stargazers)

[What is Floci AZ?](#what-is-floci-az) · [Quick Start](#quick-start) · [Features](#features) · [Why Floci AZ?](#why-floci-az) · [Services](#supported-services) · [SDKs](#sdk-integration) · [Compatibility](#compatibility-testing) · [Configuration](#configuration) · [Docs](https://floci.io/floci-az/)

---

## What is Floci AZ?

[](#what-is-floci-az)

Floci AZ is a free, open-source local Azure emulator for development, testing, and CI.

It gives you Azure-compatible services on your machine without requiring a cloud account, an auth token, or paid feature gates. Point your Azure SDK, CLI, or Terraform at `http://localhost:4577` and keep your existing workflows.

Floci AZ is the Azure member of the [Floci](https://github.com/floci-io) emulator family.

| Emulator | Cloud | Port |
| --- | --- | --- |
| [floci](https://github.com/floci-io/floci) | AWS | 4566 |
| **[floci-az](https://github.com/floci-io/floci-az)** | **Azure** | **4577** |
| [floci-gcp](https://github.com/floci-io/floci-gcp) | GCP | 4588 |
| [floci-oci](https://github.com/floci-io/floci-oci) | OCI | 4599 |

## Quick Start

[](#quick-start)

```
# docker-compose.yml
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock  # required for Azure Functions
```

```
docker compose up
```

Or run directly:

```
docker run -d --name floci-az \
  -p 4577:4577 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  floci/floci-az:latest
```

All services are available at `http://localhost:4577`. Use any account name and key: SharedKey credentials are not validated.

Open `http://localhost:4577/_floci/ui` in a browser for the [web console](https://floci.io/floci-az/ui/), which floci-az starts on demand.

> **Azure Functions** requires access to the Docker socket so Floci AZ can spawn runtime containers on demand. Mount `/var/run/docker.sock` as shown above. If you don't use Functions, the socket mount is optional.

**TLS (for the Cosmos DB Java SDK)**

## Features

[](#features)

**Local Azure without the cloud account**

Run Azure-compatible services locally without an Azure account, auth token, or paid feature gates.

**Real Docker where fidelity matters**

**Drop-in Azure SDK compatibility**

**Fast enough for CI**

**Configurable persistence**

## Why Floci AZ?

[](#why-floci-az)

Floci AZ gives you more services than the official local tools, consolidated on a single port, with a native image that starts in milliseconds.

| Feature | Floci AZ | [Azurite](https://github.com/Azure/Azurite) | [Functions Core Tools](https://github.com/Azure/azure-functions-core-tools) |
| --- | --- | --- | --- |
| Blob Storage | ✅ | ✅ | ❌ |
| Queue Storage | ✅ | ✅ | ❌ |
| Table Storage | ✅ | ✅ | ❌ |
| Azure Functions | ✅ | ❌ | ✅ |
| App Configuration | ✅ | ❌ | ❌ |
| Cosmos DB (SQL API) | ✅ | ❌ | ❌ |
| Key Vault | ✅ | ❌ | ❌ |
| Event Hubs | ✅ | ❌ | ❌ |
| Service Bus | ✅ | ❌ | ❌ |
| Azure SQL Database | ✅ | ❌ | ❌ |
| Azure DB for PostgreSQL | ✅ | ❌ | ❌ |
| AKS (Kubernetes) | ✅ | ❌ | ❌ |
| Container Apps | ✅ | ❌ | ❌ |
| API Management | ✅ | ❌ | ❌ |
| Virtual Machines | ✅ | ❌ | ❌ |
| Azure Cache for Redis | ✅ | ❌ | ❌ |
| Container Registry | ✅ | ❌ | ❌ |
| Container Instances | ✅ | ❌ | ❌ |
| Event Grid | ✅ | ❌ | ❌ |
| Azure Monitor / Logs | ✅ | ❌ | ❌ |
| Communication Email | ✅ | ❌ | ❌ |
| Managed Identity | ✅ | ❌ | ❌ |
| Template Deployments | ✅ | ❌ | ❌ |
| Azure Policy | ✅ | ❌ | ❌ |
| Azure Resource Graph | ✅ | ❌ | ❌ |
| Management Groups | ✅ | ❌ | ❌ |
| Microsoft Entra ID | ✅ | ❌ | ❌ |
| Microsoft Graph | ✅ | ❌ | ❌ |
| Native binary | ✅ | ❌ | ✅ |
| Unified port | ✅ (4577) | ❌ | ❌ |
| Storage modes | ✅ (persistent/WAL/Hybrid) | ❌ | ❌ |
| Startup time | **\<100ms** (native image) | Moderate | Fast |
| License | **MIT** | MIT | MIT |

**Azure Cosmos DB Emulator vs Floci AZ**

## Architecture Overview

[](#architecture-overview)

```
flowchart LR
    Client["Azure SDK / CLI"]

    subgraph FlociAz ["floci-az, port 4577"]
        Router["HTTP Router\nJAX-RS / Vert.x\nprotocol-sniffing TLS proxy"]

        subgraph Stateless ["Stateless Services"]
            A["App Configuration · Key Vault\nAPI Management · Event Grid\nVirtual Network · Virtual Machines\nContainer Instances · Monitor / Log Analytics\nCommunication Email\nManaged Identity · Azure Policy · Management Groups · Resource Graph · Microsoft Entra ID · Microsoft Graph · ARM management plane"]
        end

        subgraph Stateful ["Stateful Services"]
            B["Blob · Queue · Table\nCosmos DB (NoSQL)"]
        end

        subgraph Containers ["Container Services"]
            C["Azure Functions\nEvent Hubs\nService Bus\nAzure SQL\nAKS\nAzure Cache for Redis\nContainer Registry"]
            D["Cosmos engines\nmongo · citus · scylla · gremlin"]
        end

        Router --> Stateless
        Router --> Stateful
        Router --> Containers
        Stateless & Stateful --> Store[("StorageBackend\nmemory · hybrid · persistent · wal")]
    end

    Docker["Docker Engine"]
    Client -->|"HTTP/HTTPS :4577\nAzure wire protocol"| Router
    Containers -->|"Docker API\nsidecars & function containers"| Docker
```

Loading

## Supported Services

[](#supported-services)

| Service | Routing | Notable operations |
| --- | --- | --- |
| **Blob Storage** | `/{account}/` | Create/delete containers, upload/download/delete blobs, list blobs; ADLS Gen2 DFS filesystem/path operations with Hadoop ABFS 3\.3.4 compatibility; user delegation key/SAS |
| **Azure SignalR Service** | `/server/`, `/client/`, `*.service.signalr.net` | Default mode over WebSockets; ASP.NET Core hubs, JSON/MessagePack, groups, users, and broadcasts. Per-account isolation through `{account}.service.signalr.net` hostnames |
| **Queue Storage** | `/{account}-queue/` | Create/delete queues, send/receive/peek/delete messages, visibility timeout |
| **Table Storage** | `/{account}-table/` | Create/delete tables, insert/get/update/upsert/delete entities; OData `$filter` / `$select` / `$top`; server-side pagination (continuation tokens); ETag optimistic concurrency; Entity Group Transactions (`$batch`) |
| **Azure Functions** | `/{account}-functions/` | Deploy & invoke HTTP-triggered functions (node, python, java, dotnet); warm-container pool |
| **App Configuration** | `/{account}-appconfig/` | Key-values, labels, feature flags, snapshots (async provisioning), revisions, locks, ETags; pagination (`@nextLink`), `$select`, `tags` filtering, `Accept-Datetime` time-travel, `Sync-Token` |
| **Cosmos DB (NoSQL)** | `/{account}-cosmos/` | Databases, containers, documents CRUD \+ full SQL queries: always-on, no Docker. PATCH; transactional batch. |
| **Cosmos DB NoSQL (embedded)** | `/{account}-cosmos-nosql/` | Same embedded SQL engine as above, exposed as a named engine endpoint. Opt-in with `FLOCI_AZ_SERVICES_COSMOS_ENGINES_NOSQL_ENABLED=true`; no Docker required. |
| **Key Vault** | `/{account}-keyvault/` | Self-signed certificates with PFX/PEM secrets; Secrets CRUD, versioning, soft-delete, properties update; Keys CRUD, backup/restore, rotation, RSA/EC/oct crypto (encrypt/decrypt/sign/verify/wrap/unwrap), `/rng`; Managed HSM (`/{account}-managedhsm/`) |
| **Event Hubs** | AMQP `:5672` / Kafka `:9093` | AMQP 1\.0 (Artemis sidecar), Kafka-compatible (Redpanda, opt-in) |
| **Service Bus** | `/{account}-servicebus/` \+ AMQP `:5673` | Queues, topics, subscriptions (created dynamically); AMQP 1\.0 data plane via Artemis sidecar, or mocked (management plane only) |
| **Azure SQL Database** | ARM path \+ `/{account}-sql/` | Servers, databases, firewall rules; ARM-only by default, optional managed SQL Server 2025 containers |
| **Azure Database for PostgreSQL** | ARM path (`Microsoft.DBforPostgreSQL`) \+ `/{account}-postgres/` | Flexible servers, databases, firewall rules, configurations; Docker-backed `postgres:17-alpine` containers (no EULA), dynamic port allocation, or mocked |
| **Azure Database for MySQL** | ARM path (`Microsoft.DBforMySQL`) \+ `/{account}-mysql/` | Flexible servers, databases, firewall rules, configurations; Docker-backed `mysql:8.0` containers (no EULA), dynamic port allocation, or mocked |
| **Azure Database for MariaDB** | ARM path (`Microsoft.DBforMariaDB`) \+ `/{account}-mariadb/` | Servers (single-server model), databases, firewall rules, configurations; Docker-backed `mariadb:10.11` containers (no EULA), dynamic port allocation, or mocked |
| **Azure Kubernetes Service** | ARM path (`Microsoft.ContainerService`) | CreateOrUpdate, Get, Delete, List, agent pools, kubeconfig (`listClusterAdminCredential`); real k3s containers or mocked |
| **Azure Container Apps** | ARM path (`Microsoft.App`) \+ environment-specific app FQDN | Managed environment/app CRUD, Single/Multiple revisions, secrets, min/max replicas, Docker-backed HTTP ingress or mocked |
| **API Management** | ARM path (`Microsoft.ApiManagement`) \+ `/{account}-apim/{service}/` | In-process APIM emulator for ARM resources, gateway routing, products/subscriptions, named values, backends, OpenAPI import, and a focused policy subset |
| **Virtual Network** | ARM path (`Microsoft.Network`) | VNet, subnet, NIC, public IP, NSG, private DNS zone (+ virtual network links, record sets), private endpoint (+ private DNS zone groups), and private link service ARM resources; subnet listing is scoped to the parent VNet; NIC private IPs are synthesized for VM/Terraform compatibility |
| **Virtual Machines** | ARM path (`Microsoft.Compute`) | VM lifecycle (create/start/stop/deallocate/restart/delete/list), instanceView power state; mocked by default (no Docker), or one Linux container per VM with `mocked: false` |
| **Azure Cache for Redis** | ARM path (`Microsoft.Cache`) | Cache CRUD, `listKeys`/`regenerateKey`; real `valkey/valkey:8-alpine` containers (data plane, primary key as password) or mocked; non-SSL port |
| **Azure Container Registry** | ARM path (`Microsoft.ContainerRegistry`) | Registry CRUD, `listCredentials`/`regenerateCredential`, `checkNameAvailability`; one shared `registry:2` (Docker Registry V2 push/pull, anonymous on its published port); `az acr login` through the Entra token exchange on `{name}.azurecr.io`; or mocked |
| **Azure Container Instances** | ARM path (`Microsoft.ContainerInstance`) | Container group lifecycle (create/update/delete/list by rg \+ subscription), `start`/`stop`/`restart` with spec-exact LRO shapes, container logs, instanceView, azurerm-safe read-backs (ports/resources always present, canonical enum casing, secrets never echoed); real Docker containers with shared-netns pod semantics (`localhost` between group members) or mocked |
| **Event Grid** | ARM path (`Microsoft.EventGrid`) \+ `/{topic}-eventgrid/api/events` | Custom Topics, `listKeys`/`regenerateKey`, webhook `eventSubscriptions` with subject/eventType filters; publish in Event Grid \+ CloudEvents 1\.0 schemas; async webhook delivery with retry; `SubscriptionValidationEvent` handshake; HTTP-only (no sidecar) |
| **Azure Monitor / Log Analytics** | ARM path (`Microsoft.OperationalInsights` / `Microsoft.Insights`) \+ `/dataCollectionRules/{id}/streams/{stream}` \+ `/v1/workspaces/{id}/query` | Workspaces, Data Collection Endpoints/Rules; Logs Ingestion API; Log Analytics query with a KQL subset (`where`/`project`/`take`/`limit` \+ timespan); subscription Activity Log with `Administrative` entries for every ARM write, delete and action (`$filter`, `$select`, `nextLink` paging); HTTP-only (no sidecar) |
| **Communication Services Email** | `/emails:send` \+ `/emails/operations/{id}` \+ `/emailMessages` \+ ARM path (`Microsoft.Communication`) | ACS Email send \+ status polling; in-memory inspection mailbox (Mailpit-style `GET /emailMessages`); communication/email services \+ domains via ARM; captures messages locally, no real delivery; HTTP-only (no sidecar) |
| **Managed Identity** | ARM path (`Microsoft.ManagedIdentity`) \+ `/metadata/identity/oauth2/token` | User-assigned identities (server-generated `principalId`/`clientId`), federated identity credentials, system-assigned `identities/default`; IMDS token endpoint for `ManagedIdentityCredential` (point the SDK at the emulator with `AZURE_POD_IDENTITY_AUTHORITY_HOST`); v1.0 JWTs signed with the Entra key, verifiable via JWKS; HTTP-only (no sidecar) |
| **Microsoft Entra ID** | `/{tenant}/oauth2/...` \+ `/.well-known/openid-configuration` | OpenID Connect provider: RS256-signed v1.0 and v2.0 tokens, discovery, JWKS; `client_credentials`, `password` (ROPC) and authorization code \+ PKCE grants; scoped OIDC UserInfo (`/oidc/userinfo`); Azure-shaped `AADSTS` errors; client secrets accepted as sent by default, checked against the directory with `validate-client-credentials: true`; HTTP-only (no sidecar) |
| **Microsoft Graph** | `/v1.0/...` | Narrow slice sharing the Entra directory: service principal discovery (azurerm bootstrap), groups with `$filter` and `$expand=members`, group membership (`getMemberGroups`, `members/$ref`), app registrations with client secrets (`applications`, `addPassword` / `removePassword`); full Graph CRUD out of scope; HTTP-only (no sidecar) |
| **Azure Resource Manager** | `/subscriptions/...` \+ `/providers/...` | Tenants and subscriptions (`GET /tenants`, `GET /subscriptions`, registered on first use or refused under `parity: strict`), locations, resource groups (CRUD, `HEAD`, tags, `managedBy`), resource listing and `checkNameAvailability`, resource providers with `register` / `unregister`; ARM shells for storage accounts, key vaults and managed HSMs, and `Microsoft.Web/sites`; management-plane fallthrough for the other `Microsoft.*` providers; HTTP-only (no sidecar) |
| **ARM Template Deployments** | ARM path (`Microsoft.Resources/deployments`) | `az deployment group create` and `azurerm_resource_group_template_deployment` at resource-group scope, and `az deployment sub create`: each resource created through the real ARM handlers, `dependsOn` and implicit `reference()` ordering, outputs read live, `validate`, `exportTemplate`, `cancel`; asynchronous with Azure's `operationStatuses` polling; `copy` loops; nested deployments (Bicep modules); linked templates (`templateLink`, `parametersLink`, `relativePath`); HTTP-only (no sidecar) |
| **Azure Policy** | ARM path (`Microsoft.Authorization/policy*`) | Policy definitions, set definitions (initiatives), assignments and exemptions at management-group, subscription, resource-group and resource scope; `$filter` scope forms (`atScope()`, `atExactScope()`, `atScopeAndBelow()`); server-side `scope`, `instanceId`, `definitionVersion`, audit metadata and assignment identities; referential integrity on delete. Assigned rules are evaluated on every resource write: `deny` answers `RequestDisallowedByPolicy`, `append` amends the request, `audit` is recorded (`/_admin/policy/evaluations`); compliance states, summaries and on-demand `triggerEvaluation` scans through `Microsoft.PolicyInsights`; no built-ins seeded; HTTP-only (no sidecar) |
| **Azure Resource Graph** | ARM path (`Microsoft.ResourceGraph/resources`) | KQL queries over the emulator's `resources`, `resourcecontainers` and `policyresources` tables: the pipe operators, comparison and string operators, conversion and string functions Resource Graph clients send; `objectArray` and `table` result formats; `$top` / `$skip` / `$skipToken` paging; Azure's `BadRequest` details for invalid queries; HTTP-only (no sidecar) |
| **Azure Management Groups** | ARM path (`Microsoft.Management/managementGroups`) | Tenant root group, groups with `$expand=children\|path\|ancestors` and `$recurse`, patch (display name, parent), delete, descendants, subscription membership, `getEntities` with parent chains and counts, `checkNameAvailability`; HTTP-only (no sidecar) |

**API Management details**

## Real Docker Integration

[](#real-docker-integration)

Floci AZ uses real Docker containers when in-process emulation would reduce fidelity.

| Service | Default image | What is real |
| --- | --- | --- |
| Azure Functions | `mcr.microsoft.com/azure-functions/<runtime>` | Real Azure Functions runtime, warm container pool |
| Event Hubs AMQP | `apache/activemq-artemis` | Full AMQP 1\.0 broker |
| Event Hubs Kafka | `redpandadata/redpanda` | Kafka-compatible broker (opt-in) |
| Cosmos DB MongoDB | `mongo:7` | MongoDB Community Server, full wire protocol |
| Cosmos DB PostgreSQL | `citusdata/citus` | Citus: the exact engine Azure runs |
| Cosmos DB Cassandra | `scylladb/scylla:6.2` | CQL-compatible drop-in |
| Cosmos DB Gremlin | `tinkerpop/gremlin-server` | Apache TinkerPop: standard Gremlin traversals |
| Azure SQL Database | `mcr.microsoft.com/mssql/server:2025-latest` | Optional managed SQL Server engine (per server) |
| Azure Database for PostgreSQL | `postgres:17-alpine` | PostgreSQL engine (per flexible server) |
| Azure Database for MySQL | `mysql:8.0` | MySQL engine (per flexible server) |
| Azure Database for MariaDB | `mariadb:10.11` | MariaDB engine (per server) |
| AKS | `rancher/k3s:latest` | Kubernetes API server via k3s |
| Azure Container Apps | User-provided images | Revision replicas with HTTP ingress through floci-az |
| Azure Cache for Redis | `valkey/valkey:8-alpine` | Redis / Valkey protocol (per cache) |
| Azure Container Registry | `registry:2` | OCI-compatible registry for docker push and docker pull (shared) |

Docker-backed services require the Docker socket:

```
docker run -d --name floci-az \
  -p 4577:4577 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  floci/floci-az:latest
```

The Cosmos DB multi-API engines (MongoDB, PostgreSQL, Cassandra, Gremlin, plus the embedded NoSQL and Table engines) are disabled by default and configured separately: see the [Cosmos DB engine configuration](https://floci.io/floci-az/services/cosmos/#multi-api-engines) docs.

## Persistence and Storage Modes

[](#persistence-and-storage-modes)

Floci AZ features the same flexible storage architecture as Floci. Configure the storage mode globally via `FLOCI_AZ_STORAGE_MODE` or override it per service.

| Mode | Behavior | Best for... | Durability |
| --- | --- | --- | --- |
| **`memory`** **(Default)** | Entirely in-RAM. Data is lost when the container stops. | Speed, ephemeral testing, CI pipelines. | ❌ None |
| **`persistent`** | Data is loaded at startup and flushed to disk on graceful shutdown. | Simple local dev with state preservation. | ⚠️ Medium |
| **`hybrid`** | In-memory performance with periodic async flushing (every 5s). | The perfect balance of speed and safety. | ✅ Good |
| **`wal`** | Write-Ahead Log. Every mutation is logged to disk before responding. | Maximum durability for critical state. | 💎 Highest |

Tip

Use **`hybrid`** for a "it just works" experience that survives container restarts. For ephemeral integration tests where state doesn't matter, keep the default **`memory`** mode for maximum performance.

## SDK Integration

[](#sdk-integration)

Floci AZ uses path-style routing:

| Service | Endpoint | Notes |
| --- | --- | --- |
| Blob | `http://localhost:4577/{accountName}` |  |
| Data Lake Storage Gen2 | `http://localhost:4577/{accountName}` | Use the `{accountName}.dfs.core.windows.net` Host header; maps to the Blob backend and supports Hadoop ABFS 3\.3.4 filesystem/path flows, including HNS detection, conditional create/overwrite, properties/XAttrs, ACL metadata, leases, append/flush, listing, status, rename, and delete |
| Queue | `http://localhost:4577/{accountName}-queue` |  |
| Table | `http://localhost:4577/{accountName}-table` |  |
| Functions | `http://localhost:4577/{accountName}-functions` |  |
| App Configuration | `http://localhost:4577/{accountName}-appconfig` | Some SDKs require an `https://` URL: use a `ForceHttp` transport policy to rewrite to HTTP |
| Cosmos DB | `http://localhost:4577/{accountName}-cosmos` | Python / Node SDKs. **Java SDK:** enable TLS (`FLOCI_AZ_TLS_ENABLED=true`) and use `https://localhost:4577`: the SDK enforces TLS; cert auto-generated at runtime, fetch from `GET /_floci/tls-cert` |
| Key Vault | `http://localhost:4577/{accountName}-keyvault` | Some SDKs require an `https://` URL: use a `ForceHttp` transport policy to rewrite to HTTP |
| Event Hubs | AMQP `amqp://localhost:5672` · Kafka `localhost:9093` |  |

The standard development storage connection string works out of the box:

```
DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:4577/devstoreaccount1;QueueEndpoint=http://localhost:4577/devstoreaccount1-queue;TableEndpoint=http://localhost:4577/devstoreaccount1-table;
```

**Python**

**Java**

**Node.js / TypeScript**

**Azure Functions**

**Azure CLI (azfloci)**

**When your app runs in a separate container**

## Compatibility Testing

[](#compatibility-testing)

The [`compatibility-tests`](/floci-io/floci-az/blob/main/compatibility-tests) directory validates Floci AZ across SDKs and tooling workflows. Each directory is one suite. App Configuration, Key Vault, Event Hubs, and Service Bus are exercised inside the SDK suites rather than as standalone modules.

| Module | Language / Tool | Coverage |
| --- | --- | --- |
| `sdk-test-python` | Python 3 | Blob, Queue, Table, Cosmos, App Configuration, Key Vault, ACR, Redis, VM, Managed Identity |
| `sdk-test-java` | Java 21 | Storage, Cosmos (+ Mongo/PostgreSQL/Cassandra/Gremlin/Table/NoSQL engines), App Config, Key Vault, Event Hubs, Service Bus, Functions, Container Apps, API Management, SQL, PostgreSQL (Flexible Server), Managed Identity, Policy |
| `sdk-test-node` | Node.js | App Configuration, Blob, Cosmos, Event Hubs, Key Vault, Queue, Table, Service Bus, Entra ID, Managed Identity |
| `sdk-test-dotnet` | .NET | Blob service SAS, Cosmos (+ bracket properties, point partition, transactional batch), Key Vault (+ certificates, timestamps), Service Bus (+ lock renewal, metadata, peek-lock), SignalR. The only suite covering SignalR |
| `sdk-test-cpp` † | C++ 17 | Blob, Queue: Lifecycle plus the bodyless-error path no other SDK reproduces |
| `compat-terraform` | Terraform | `azurerm` provider apply/destroy (resource group, storage, key vault, VNet, VM, Redis, ACR) |
| `compat-opentofu` | OpenTofu | Same `azurerm` suite via `tofu`, plus PostgreSQL Flexible Server (server \+ database) |
| `compat-azcli` ‡ | Azure CLI | `az` against a custom cloud with Entra service-principal login |

† Built from source via vcpkg, so the image takes \~6 min cold (seconds if warm).

‡ Ships with the Entra ID feature ([\#23](https://github.com/floci-io/floci-az/issues/23)).

Run the suites against a running container:

```
make test-python-compat
make test-java-compat
make test-node-compat
make test-cpp-compat
make test-cosmos-all    # Cosmos engine tests: MongoDB · PostgreSQL · Cassandra · Gremlin · Table · NoSQL (requires Docker)
make test-iac-compat    # Terraform + OpenTofu
make compat-docker      # full matrix
```

## Migrating from Azurite

[](#migrating-from-azurite)

Azurite users can point their existing connection strings at Floci AZ with a single endpoint change.

```
# Before (Azurite default)
DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02...;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;QueueEndpoint=http://127.0.0.1:10001/devstoreaccount1;TableEndpoint=http://127.0.0.1:10002/devstoreaccount1;

# After (Floci AZ, single port)
DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02...;BlobEndpoint=http://localhost:4577/devstoreaccount1;QueueEndpoint=http://localhost:4577/devstoreaccount1-queue;TableEndpoint=http://localhost:4577/devstoreaccount1-table;
```

The account name and key are the same. Floci AZ consolidates all services onto port `4577`: no more separate ports per service.

## Image Tags

[](#image-tags)

All images are native (multi-arch `linux/amd64` \+ `linux/arm64`).

| Channel | Standard | Compat with Azure CLI and azfloci |
| --- | --- | --- |
| Release, floating | `latest` | `latest-compat` |
| Release, pinned | `x.y.z` | `x.y.z-compat` |
| Nightly, floating | `nightly` | `nightly-compat` |
| Nightly, dated | `nightly-mmddyyyy` | `nightly-mmddyyyy-compat` |

Use `latest` for stable releases, a pinned version for reproducible builds, and `nightly` to track `main`.

```
image: floci/floci-az:latest         # recommended
image: floci/floci-az:latest-compat  # includes Azure CLI and azfloci
image: floci/floci-az:x.y.z          # pinned release
image: floci/floci-az:nightly        # track main
```

### Release train

[](#release-train)

Stable releases ship on the **15th and the last day of each month**, cut automatically at 00:30 US Central time. Between trains, `floci/floci-az:nightly` tracks `main`. Every merged fix is available the next day, and dated `nightly-mmddyyyy` tags let you pin a specific night's build.

Versions are derived from Conventional Commits by [semantic-release](https://github.com/semantic-release/semantic-release); `CHANGELOG.md` is generated, never hand-edited. Releases are cut from `main` only: there are no maintenance branches.

## Configuration

[](#configuration)

All settings are overridable via environment variables (`FLOCI_AZ_` prefix).

| Variable | Default | Description |
| --- | --- | --- |
| `FLOCI_AZ_PORT` | `4577` | Port exposed by the API |
| `FLOCI_AZ_BASE_URL` | `http://localhost:4577` | Base URL used when the emulator returns service URLs |
| `FLOCI_AZ_HOSTNAME` | Unset | Hostname added to returned URLs and TLS cert SANs when running inside Docker Compose |
| `FLOCI_AZ_TLS_ENABLED` | `false` | Enable HTTP+HTTPS on the same port via protocol-sniffing proxy |
| `FLOCI_AZ_PARITY` | `compat` | How closely the ARM platform answers follow Azure: `compat` keeps every response as floci-az has always given it, `azure` gives Azure's answers (resource-group update and cascading delete, listed subscriptions), `strict` adds Azure's refusals (unknown subscription or resource group). See the [ARM docs](/floci-io/floci-az/blob/main/docs/services/arm.md#parity-levels) |
| `FLOCI_AZ_STORAGE_MODE` | `memory` | Storage mode: `memory`, `persistent`, `hybrid`, or `wal` |
| `FLOCI_AZ_STORAGE_PATH` | `/app/data` | Directory used for persisted state |
| `FLOCI_AZ_DOCKER_DOCKER_HOST` | `unix:///var/run/docker.sock` | Docker socket used to spawn function and sidecar containers |
| `FLOCI_AZ_DOCKER_ENDPOINT_MODE` | `auto` | How sidecar containers are addressed: `auto` (container name or IP and internal port inside Docker, `localhost` and the published port otherwise) or `published` (the Docker daemon's host and the published port, for remote daemons such as docker-in-docker or kubedock) |
| `FLOCI_AZ_DOCKER_RESOURCE_NAMESPACE` | *(unset)* | Namespace inserted into the name of every container and volume floci-az creates (`floci-az-<ns>-...`) and added as the `floci_namespace` label, so parallel emulator instances on one Docker host don't collide |

Per-service enable flags (`FLOCI_AZ_SERVICES_<SERVICE>_ENABLED`), TLS certificate paths, Cosmos engine settings, and the rest are documented in the full reference.

Full reference: [configuration docs](https://floci.io/floci/configuration/environment-variables/)

### Per-service storage override

[](#per-service-storage-override)

You can set a different storage mode for each service independently:

```
# docker-compose.yml
environment:
  FLOCI_AZ_STORAGE_MODE: memory
  FLOCI_AZ_STORAGE_SERVICES_BLOB_MODE: wal
```

### Multi-container Docker Compose

[](#multi-container-docker-compose)

When your application runs in a separate container, use the service name as the hostname so returned URLs (such as blob/queue/table endpoints) resolve correctly:

```
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock  # required for Azure Functions
    networks:
      - app-net

  my-app:
    environment:
      AZURE_BLOB_ENDPOINT: http://floci-az:4577/devstoreaccount1
      AZURE_QUEUE_ENDPOINT: http://floci-az:4577/devstoreaccount1-queue
      AZURE_TABLE_ENDPOINT: http://floci-az:4577/devstoreaccount1-table
      AZURE_FUNCTIONS_ENDPOINT: http://floci-az:4577/devstoreaccount1-functions
      AZURE_APPCONFIG_ENDPOINT: https://floci-az:4577/devstoreaccount1-appconfig  # https required by App Config SDK (use ForceHttp transport in your client)
    depends_on:
      floci-az:
        condition: service_healthy
    networks:
      - app-net

networks:
  app-net:
```

For the **Cosmos DB multi-API engines** (MongoDB, PostgreSQL, Cassandra, Gremlin, and the embedded NoSQL and Table engines), including enable flags, image/port overrides, the `/connect` endpoint, and SDK examples, see the [Cosmos DB engine configuration](https://floci.io/floci-az/services/cosmos/#multi-api-engines) docs.

**Cosmos DB API parity rationale**

## Non-Goals and Constraints

[](#non-goals-and-constraints)

To prevent configuration errors, note what Floci AZ **does not** do:

1. **HTTPS (most services):** All services run on plain HTTP on port `4577` by default. Do not use `DefaultEndpointsProtocol=https` unless you have explicitly enabled TLS. For SDKs that require an `https://` URL (App Configuration, Key Vault), use a `ForceHttp` transport policy to rewrite the request back to HTTP before it is sent.

2. **TLS (optional):** Set `FLOCI_AZ_TLS_ENABLED=true` to enable HTTP+HTTPS on the same port `4577` via a protocol-sniffing proxy. A self-signed certificate is generated at runtime and persisted under `data/tls/`; it regenerates automatically when `FLOCI_AZ_HOSTNAME` or `FLOCI_AZ_BASE_URL` changes. Fetch the active certificate PEM from `GET /_floci/tls-cert` to install it dynamically into your truststore. The **Azure Cosmos DB Java SDK** requires TLS. Enable this when using it.

3. **No Web UI:** There is no dashboard at `4577`. It is an API-only emulator.

4. **Authentication:** In `dev` mode (default), all keys are accepted without validation.

5. **Production Scale:** Designed for dev/test. Not for high-availability storage.

## Community

[](#community)

Join the Floci community on [Slack](https://join.slack.com/t/floci/shared_invite/zt-3tjn02s3q-A00kEjJ1cZxsg_imTfy6Cw) or [GitHub Discussions](https://github.com/orgs/floci-io/discussions). Feature ideas, compatibility questions, design tradeoffs, and rough proposals are welcome.

## Sponsors

[](#sponsors)

Floci is independent open source, funded by the people and companies who use it. Sponsorship buys gratitude and nothing else: every emulated service is free for everyone, forever, and no sponsor gets features, priority, or roadmap influence that the rest of the Flock does not.

### 🥇 Gold

[](#-gold)

Large logo with top placement in the emulator READMEs and on floci.io, plus a mention in release notes.

[IceGuard](https://github.com/iceguard) · [Softmax](https://softmax.com/)

### 🥈 Silver

[](#-silver)

Logo in the emulator READMEs and on floci.io, plus a mention in release notes.

*Your logo here. [Become a sponsor](https://github.com/sponsors/floci-io).*

### 🥉 Community

[](#-community)

Name in the emulator READMEs, a sponsor badge on GitHub, and our sincere thanks.

[AutoScout24](https://www.autoscout24.com) · [Nexxion AI](https://nexxion.ai/)

Every sponsor, including the Friends of the Flock who support Floci outside these tiers, is listed in [THANKS.md](https://github.com/floci-io/.github/blob/main/THANKS.md).

**[Sponsor Floci](https://github.com/sponsors/floci-io)**

## Star History

[](#star-history)

[ ![Star History Chart](https://camo.githubusercontent.com/6fd26c07ce821e02989cf18aa52ad6aa81ec45af3197c60141804eb53babe8bb/68747470733a2f2f6170692e737461722d686973746f72792e636f6d2f63686172743f7265706f733d666c6f63692d696f2f666c6f63692d617a26747970653d64617465266c6567656e643d746f702d6c656674)](https://www.star-history.com/?repos=floci-io%2Ffloci-az&type=date&legend=top-left)

## Contributors

[](#contributors)

[ ![Contributors](https://camo.githubusercontent.com/eb8d2e7f422257907421db010cb8c2fcb5b9f14f1aaa419a92ea0f0c11299f7b/68747470733a2f2f636f6e747269622e726f636b732f696d6167653f7265706f3d666c6f63692d696f2f666c6f63692d617a266d61783d31303026636f6c756d6e733d3230)](https://github.com/floci-io/floci-az/graphs/contributors)

## License

[](#license)

MIT. Use it however you want.

## About

Light, fluffy, and always free \- Local Azure Emulator

[floci.io/floci-az/](https://floci.io/floci-az/)

### Topics

[azure](/topics/azure)[azure-devops](/topics/azure-devops)[azure-functions](/topics/azure-functions)[azure-functions-core-tools](/topics/azure-functions-core-tools)[azure-service](/topics/azure-service)[azure-storage](/topics/azure-storage)[azurite](/topics/azurite)[docker](/topics/docker)[emulator](/topics/emulator)[floci](/topics/floci)[floci-az](/topics/floci-az)[testcontainers](/topics/testcontainers)

### Resources

[Readme](#readme-ov-file)

[MIT license](#MIT-1-ov-file)

### Code of conduct

[Code of conduct](/floci-io/floci-az#coc-ov-file)

### Contributing

[Contributing](#contributing-ov-file)

### Security policy

[Security policy](#security-ov-file)

[Activity](/floci-io/floci-az/activity)

[Custom properties](/floci-io/floci-az/custom-properties)

### Stars

**814** stars

### Watchers

**3** watching

### Forks

[**144** forks](/floci-io/floci-az/forks)

[Report repository](/contact/report-content?content_url=https%3A%2F%2Fgithub.com%2Ffloci-io%2Ffloci-az&report=floci-io+%28user%29)

## Releases

## Sponsor this project

## Contributors

## Languages

## Footer

[](https://github.com) © 2026 GitHub, Inc.

### Footer navigation

- [Terms](https://docs.github.com/site-policy/github-terms/github-terms-of-service)

- [Privacy](https://docs.github.com/site-policy/privacy-policies/github-privacy-statement)

- [Security](https://github.com/security)

- [Status](https://www.githubstatus.com/)

- [Community](https://github.community/)

- [Docs](https://docs.github.com/)

- [Contact](https://support.github.com?tags=dotcom-footer)

- Manage cookies

- Do not share my personal information

---

## Assistant

_Thinking:_

The repo is essentially empty (only README "# La-Tiendita" and .atl directory). User wants a docker-compose file to run floci-az in their repo, with microservices and object storage in mind.

I'm in plan mode. I should discuss the plan. Key facts from README:

- docker-compose.yml with floci/floci-az:latest, port 4577, docker.sock mount (optional if no Functions)
- storage mode: hybrid recommended for dev
- connection string for devstoreaccount1
- microservices: they'll have separate services connecting to floci-az; can add networkQuestions to ask: does he want just the compose file, or also .env.example, healthcheck, and microservice scaffolding? Also whether he'll use Azure Functions. Also language of microservices (repo is empty).

Note Windows: docker.sock mount path — on Windows with Docker Desktop (WSL2), /var/run/docker.sock works in compose. Yes, Docker Desktop exposes /var/run/docker.sock.

Plan: create docker-compose.yml at repo root with floci-az, healthcheck, volume for persistence (hybrid mode), .env.example, README section. Maybe a `infra/` folder.

The persona says: ask at most one question at a time, stop and wait. Also plan mode: discuss plan.

Let me propose the plan concisely and ask one question — the key fork: does he want just the emulator stack, or also example microservice config? Actually maybe the key question: will he use Azure Functions (requires docker socket) — no, better single meaningful question: scope of the file.

Actually a more meaningful fork: does he plan to run his microservices inside the same compose (multi-service dev stack) or run them locally on the host? That affects the connection string (localhost vs floci-az hostname).

Let me present the plan and ask that one question.

Verificado: floci-az es real y está activo ([floci-io/floci-az](https://github.com/floci-io/floci-az), MIT, 814 stars). Corregí algo de lo que decía el buscador antes: el README oficial ya **no** menciona el CLI `floci` como única vía — el camino oficial hoy es **Docker Compose** en el puerto **4577** (un solo puerto para Blob, Queue, Table, Functions, Cosmos, Key Vault, Service Bus, etc.).

## Qué encontré en tu repo

`C:\Users\damian\Repositorios\LaTiendita` está prácticamente vacío: solo `README.md` (`# La-Tiendita`) y una carpeta `.atl`. No hay microservicios todavía, así que el plan es crear la base desde cero.

## Plan propuesto

**1. `docker-compose.yml` en la raíz** — servicio `floci-az`:
- `image: floci/floci-az:latest` (o versión pinneada para reproducibilidad)
- Puerto `4577:4577`
- Mount de `/var/run/docker.sock` — solo lo incluyo si vas a usar **Azure Functions**; si no, es innecesario
- `FLOCI_AZ_STORAGE_MODE: hybrid` — in-memory con flush a disco cada 5s, así los datos sobreviven reinicios del contenedor (el default `memory` los pierde)
- Healthcheck + volumen para persistencia

**2. `.env.example`** — con el connection string de `devstoreaccount1` apuntando a `localhost:4577`, para que cada microservicio lo consuma sin hardcodear.

**3. Sección en el `README`** — cómo levantarlo (`docker compose up -d`), cómo verificarlo, y cómo apuntar el SDK de Azure (`BlobServiceClient`, etc.).

## Una pregunta antes de armarlo

**¿Tus microservicios van a correr dentro del mismo compose (como contenedores), o en la máquina local (node/dotnet/python directo)?**

- **En compose**: los endpoints usan el hostname `http://floci-az:4577/...`
- **En local**: usan `http://localhost:4577/...`

Esto cambia el contenido del `.env.example` y la red que defino en el compose, por eso lo pregunto antes.

---

## User

 quiero desplegarlos en floci asi que tiene que ser compatible con eso

---

## Assistant

_Thinking:_

The user says: "quiero desplegarlos en floci asi que tiene que ser compatible con eso" — they want to deploy the microservices on floci, so it has to be compatible. Hmm, "desplegarlos en floci" — deploy microservices in floci. floci-az has Azure Container Apps emulation (Docker-backed, user-provided images, revision replicas with HTTP ingress through floci-az). That's the path for "deploying microservices in floci" — Azure Container Apps emulator.

So they want microservices deployed as containers against floci-az, i.e., Container Apps emulation, meaning the compose should include a network where apps run and use `floci-az` hostname endpoints. Let me verify what floci-az's Container Apps support requires. Let me check the docs page for container apps to ground the plan.

Let me fetch the docs for container apps.

Entonces el camino es el emulador de **Azure Container Apps** de floci-az (contenedores con ingress a través de floci). Déjame verificar los requisitos reales antes de armar el plan.

**Tool: webfetch**

**Input:**
```json
{
  "format": "markdown",
  "url": "https://floci.io/floci-az/services/container-apps/"
}
```

[ Skip to content](#azure-container-apps)

[ ![logo](../../assets/floci-header.svg)](../.. "Floci-AZ")

Floci-AZ

Azure Container Apps

Initializing search

[](https://github.com/floci-io/floci-az "Go to repository")

floci-io/floci-az

- [ Home](../..)

- [ Getting Started](../../getting-started/quick-start/)

- [ Configuration](../../configuration/docker-compose/)

- [ Web Console](../../ui/)

- [ Services](../)

- [ Contributing](../../contributing/)

- [ Changelog](../../changelog/)

[ ![logo](../../assets/floci-header.svg)](../.. "Floci-AZ") Floci-AZ

[](https://github.com/floci-io/floci-az "Go to repository")

floci-io/floci-az

- [ Home](../..)

- Getting Started

  Getting Started

  - [ Quick Start](../../getting-started/quick-start/)

  - [ Installation](../../getting-started/installation/)

  - [ Azure CLI & SDK Setup](../../getting-started/azure-setup/)

  - [ Terraform / OpenTofu](../../terraform/)

- Configuration

  Configuration

  - [ Docker Compose](../../configuration/docker-compose/)

  - [ Docker Images](../../configuration/docker-images/)

  - [ Ports Reference](../../configuration/ports/)

  - [ Emulator Endpoints](../../configuration/endpoints/)

  - [ Storage Modes](../../configuration/storage/)

  - Advanced

    Advanced

    - [ application.yml Reference](../../configuration/advanced/application-yml/)

- Web Console

  Web Console

  - [ Overview](../../ui/)

  - [ Console Contract v2](../../ui/console-contract/)

- Services

  Services

  - [ Overview](../)

  - [ Azure Resource Manager](../arm/)

  - [ Blob Storage](../blob/)

  - [ Queue Storage](../queue/)

  - [ Table Storage](../table/)

  - [ Azure Functions](../functions/)

  - [ App Configuration](../app-config/)

  - [ Azure SignalR Service](../signalr/)

  - [ Key Vault](../key-vault/)

  - [ Microsoft Entra ID](../entra/)

  - [ Microsoft Graph](../graph/)

  - [ Event Hubs](../event-hub/)

  - [ Service Bus](../service-bus/)

  - [ Cosmos DB](../cosmos/)

  - [ Azure SQL Database](../sql/)

  - [ Azure Database for PostgreSQL](../postgresql/)

  - [ Azure Database for MySQL](../mysql/)

  - [ Azure Database for MariaDB](../mariadb/)

  - [ Azure Kubernetes Service](../aks/)

  - Azure Container Apps [ Azure Container Apps](./)

    Table of contents

    - [ Features](#features)

    - [ ARM endpoints](#arm-endpoints)

    - [ Example](#example)

    - [ Configuration](#configuration)

  - [ API Management](../apim/)

  - [ Virtual Network](../network/)

  - [ Virtual Machines](../vm/)

  - [ Azure Cache for Redis](../redis/)

  - [ Azure Container Registry](../acr/)

  - [ Azure Container Instances](../aci/)

  - [ Event Grid](../event-grid/)

  - [ Azure Monitor / Log Analytics](../monitor/)

  - [ Communication Services Email](../email/)

  - [ Managed Identity](../managed-identity/)

  - [ Azure Policy](../policy/)

  - [ Azure Resource Graph](../resourcegraph/)

  - [ Azure Management Groups](../managementgroups/)

  - [ ARM Template Deployments](../deployments/)

- [ Contributing](../../contributing/)

- [ Changelog](../../changelog/)

Table of contents

- [ Features](#features)

- [ ARM endpoints](#arm-endpoints)

- [ Example](#example)

- [ Configuration](#configuration)

# Azure Container Apps

Compatible with Azure Resource Manager clients using `Microsoft.App/managedEnvironments` and `Microsoft.App/containerApps`.

Real mode runs each active revision replica as Docker containers. Mocked mode keeps full ARM and revision state without starting Docker.

## Features

- Managed Environment create, get, update, delete, and list

- Container App create, get, update, delete, and list

- Versioned templates with Single and Multiple active revision modes

- Revision list, get, activate, deactivate, and restart

- Container commands, arguments, environment variables, and secret references

- External and internal HTTP ingress forwarded to running revision containers

- Weighted revision traffic and round-robin replica selection with unhealthy replica failover

- `minReplicas` and `maxReplicas` validation; local replicas start at `minReplicas`

- Scale-to-zero apps start one replica on first ingress request when `maxReplicas` permits

- Mocked mode for Docker-free tests

Scale rules beyond minimum/maximum replicas are retained in ARM responses but are not evaluated locally. Internal ingress accepts only transport peers whose source IP belongs to a Docker IPAM subnet used by a running Container App replica. Forwarded headers cannot make a public caller internal.

## ARM endpoints

```
PUT    /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/managedEnvironments/{name}
GET    /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/managedEnvironments/{name}
PATCH  /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/managedEnvironments/{name}
DELETE /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/managedEnvironments/{name}
GET    /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/managedEnvironments
GET    /subscriptions/{sub}/providers/Microsoft.App/managedEnvironments
POST   .../managedEnvironments/{name}/checkNameAvailability

PUT    /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/containerApps/{name}
GET    /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/containerApps/{name}
PATCH  /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/containerApps/{name}
DELETE /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/containerApps/{name}
POST   .../containerApps/{name}/listSecrets
GET    .../containerApps/{name}/revisions
GET    .../containerApps/{name}/revisions/{revision}
POST   .../containerApps/{name}/revisions/{revision}/activate
POST   .../containerApps/{name}/revisions/{revision}/deactivate
POST   .../containerApps/{name}/revisions/{revision}/restart
```

## Example

Create an environment:

```
curl -X PUT 'http://localhost:4577/subscriptions/dev/resourceGroups/apps/providers/Microsoft.App/managedEnvironments/local?api-version=2025-07-01' \
  -H 'Content-Type: application/json' \
  -d '{"location":"eastus","properties":{}}'
```

Create an externally accessible app:

```
curl -X PUT 'http://localhost:4577/subscriptions/dev/resourceGroups/apps/providers/Microsoft.App/containerApps/hello?api-version=2025-07-01' \
  -H 'Content-Type: application/json' \
  -d '{
    "location":"eastus",
    "properties":{
      "environmentId":"/subscriptions/dev/resourceGroups/apps/providers/Microsoft.App/managedEnvironments/local",
      "configuration":{
        "activeRevisionsMode":"Single",
        "secrets":[{"name":"token","value":"local-secret"}],
        "ingress":{"external":true,"targetPort":80}
      },
      "template":{
        "revisionSuffix":"v1",
        "containers":[{
          "name":"web",
          "image":"nginx:alpine",
          "env":[{"name":"TOKEN","secretRef":"token"}]
        }],
        "scale":{"minReplicas":1,"maxReplicas":3}
      }
    }
  }'
```

The response returns a globally unique value in `properties.configuration.ingress.fqdn`. Route that hostname to floci-az, then call it through port 4577:

```
FQDN=$(curl -s 'http://localhost:4577/subscriptions/dev/resourceGroups/apps/providers/Microsoft.App/containerApps/hello?api-version=2025-07-01' | jq -r '.properties.configuration.ingress.fqdn')
curl -H "Host: $FQDN" http://localhost:4577/
```

## Configuration

```
floci-az:
  services:
    container-apps:
      enabled: true
      mocked: true
      dns-suffix: azurecontainerapps.io
      ingress-timeout-seconds: 60
```

| Environment variable | Default | Description |
| --- | --- | --- |
| `FLOCI_AZ_SERVICES_CONTAINER_APPS_ENABLED` | `true` | Enables `Microsoft.App` routing |
| `FLOCI_AZ_SERVICES_CONTAINER_APPS_MOCKED` | `true` | Keeps ARM state without Docker containers |
| `FLOCI_AZ_SERVICES_CONTAINER_APPS_DNS_SUFFIX` | `azurecontainerapps.io` | Suffix returned in environment and app FQDNs |
| `FLOCI_AZ_SERVICES_CONTAINER_APPS_INGRESS_TIMEOUT_SECONDS` | `60` | Backend connect/request timeout |

Set `mocked: false` to enable real mode, which requires access to the Docker daemon. Template containers in one replica share the leader container's network namespace, so sidecars can communicate over `localhost`. The leader receives the dynamic host-port binding for the shared ingress target port. A replica becomes healthy only after that port accepts TCP connections. Requests still enter floci-az on port 4577\.

Made with [ Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)

---

## Assistant

_Thinking:_

Confirmed: Container Apps works. Real mode requires `mocked: false` and Docker daemon access. So the plan:

1. `docker-compose.yml` with floci-az, docker.sock mount, `FLOCI_AZ_SERVICES_CONTAINER_APPS_MOCKED=false`, storage hybrid, healthcheck.
2. `.env.example` with endpoints.
3. Deployment flow: your microservices get Dockerized (Dockerfile each), then deployed to floci-az Container Apps via ARM (az CLI with azfloci, curl, Terraform, or ARM template). The `latest-compat` tag includes Azure CLI + azfloci.

Key detail: microservice images must be loadable by the local Docker daemon (build locally, image name accessible). And routing to apps: FQDN with Host header through port 4577.

I'm in plan mode. Present the plan and ask one question at a time. One question: how do they want to deploy (Terraform/az CLI/ARM template scripts)? Or which language/stack for the microservices? The deploy mechanism matters for what files to create. Let me ask that one question.

Confirmado en la documentación oficial: Container Apps **sí está soportado** y es exactamente lo que necesitas. Dos modos:

- **`mocked: true` (default)**: solo estado ARM, no levanta contenedores reales
- **`mocked: false` (real)**: levanta réplicas como contenedores Docker reales, con ingress externo/interno, scale-to-zero, y tráfico ponderado por revisión

Para tu caso —desplegar microservicios de verdad— el modo **real** es el que va. Firma de tu stack:

```
Tus microservicios (Dockerfile por servicio)
        │  se despliegan vía ARM (Microsoft.App/containerApps)
        ▼
floci-az :4577  ←── ingress, con FQDN tipo <app>.<env>.azurecontainerapps.io
        │
Blob / Queue / Table / Cosmos (lo que usen)
```

## Plan de archivos

**1. `docker-compose.yml`** (raíz)
- `floci/floci-az:latest-compat` — incluye Azure CLI y `azfloci` para desplegar vía ARM
- `4577:4577` + mount de `/var/run/docker.sock` (**obligatorio** en modo real)
- `FLOCI_AZ_SERVICES_CONTAINER_APPS_MOCKED=false`
- `FLOCI_AZ_STORAGE_MODE=hybrid` (sobrevive reinicios)
- Healthcheck + volumen de persistencia + red `app-net`

**2. `.env.example`** — endpoints (`http://floci-az:4577/...`) y connection string de `devstoreaccount1`

**3. `infra/` con el flujo de despliegue** — un script o template que cree el *managed environment* y los *container apps* vía ARM (`Microsoft.App/...`)

**4. Sección en `README`** — flujo completo: build de imagen → deploy → invocar por FQDN a través del puerto 4577

## Una pregunta

**¿Cómo querés desplegar los microservicios al emulador?**

- **`az` CLI** (`az containerapp create` contra el emulador vía `azfloci`) — simple, script
- **Terraform/OpenTofu** (`azurerm` provider) — declarativo, ya hay compat verificada con el provider
- **ARM/Bicep templates** — lo más fiel a Azure real

Elegí uno y armo el `infra/` acorde.

---

## User

voy a usar terraform

---

## Assistant

_Thinking:_

User chose Terraform. Now I'm in plan mode — I should present the plan. Maybe verify terraform azurerm compat with floci-az container apps. There's a `compat-terraform` suite covering "azurerm provider apply/destroy (resource group, storage, key vault, VNet, VM, Redis, ACR)" — but Container Apps not explicitly listed. Let me quickly check docs terraform page to ground the plan.

Perfecto. Déjame verificar el soporte de Terraform con Container Apps en floci antes de cerrar el plan.

**Tool: webfetch**

**Input:**
```json
{
  "format": "markdown",
  "url": "https://floci.io/floci-az/terraform/"
}
```

[ Skip to content](#terraform-opentofu-azurerm-provider)

[ ![logo](../assets/floci-header.svg)](.. "Floci-AZ")

Floci-AZ

Terraform / OpenTofu

Initializing search

[](https://github.com/floci-io/floci-az "Go to repository")

floci-io/floci-az

- [ Home](..)

- [ Getting Started](../getting-started/quick-start/)

- [ Configuration](../configuration/docker-compose/)

- [ Web Console](../ui/)

- [ Services](../services/)

- [ Contributing](../contributing/)

- [ Changelog](../changelog/)

[ ![logo](../assets/floci-header.svg)](.. "Floci-AZ") Floci-AZ

[](https://github.com/floci-io/floci-az "Go to repository")

floci-io/floci-az

- [ Home](..)

- Getting Started

  Getting Started

  - [ Quick Start](../getting-started/quick-start/)

  - [ Installation](../getting-started/installation/)

  - [ Azure CLI & SDK Setup](../getting-started/azure-setup/)

  - Terraform / OpenTofu [ Terraform / OpenTofu](./)

    Table of contents

    - [ 1: Start floci-az with TLS enabled](#1-start-floci-az-with-tls-enabled)

    - [ 2: Trust the certificate](#2-trust-the-certificate)

    - [ 3: Configure the provider](#3-configure-the-provider)

    - [ Troubleshooting](#troubleshooting)

- Configuration

  Configuration

  - [ Docker Compose](../configuration/docker-compose/)

  - [ Docker Images](../configuration/docker-images/)

  - [ Ports Reference](../configuration/ports/)

  - [ Emulator Endpoints](../configuration/endpoints/)

  - [ Storage Modes](../configuration/storage/)

  - Advanced

    Advanced

    - [ application.yml Reference](../configuration/advanced/application-yml/)

- Web Console

  Web Console

  - [ Overview](../ui/)

  - [ Console Contract v2](../ui/console-contract/)

- Services

  Services

  - [ Overview](../services/)

  - [ Azure Resource Manager](../services/arm/)

  - [ Blob Storage](../services/blob/)

  - [ Queue Storage](../services/queue/)

  - [ Table Storage](../services/table/)

  - [ Azure Functions](../services/functions/)

  - [ App Configuration](../services/app-config/)

  - [ Azure SignalR Service](../services/signalr/)

  - [ Key Vault](../services/key-vault/)

  - [ Microsoft Entra ID](../services/entra/)

  - [ Microsoft Graph](../services/graph/)

  - [ Event Hubs](../services/event-hub/)

  - [ Service Bus](../services/service-bus/)

  - [ Cosmos DB](../services/cosmos/)

  - [ Azure SQL Database](../services/sql/)

  - [ Azure Database for PostgreSQL](../services/postgresql/)

  - [ Azure Database for MySQL](../services/mysql/)

  - [ Azure Database for MariaDB](../services/mariadb/)

  - [ Azure Kubernetes Service](../services/aks/)

  - [ Azure Container Apps](../services/container-apps/)

  - [ API Management](../services/apim/)

  - [ Virtual Network](../services/network/)

  - [ Virtual Machines](../services/vm/)

  - [ Azure Cache for Redis](../services/redis/)

  - [ Azure Container Registry](../services/acr/)

  - [ Azure Container Instances](../services/aci/)

  - [ Event Grid](../services/event-grid/)

  - [ Azure Monitor / Log Analytics](../services/monitor/)

  - [ Communication Services Email](../services/email/)

  - [ Managed Identity](../services/managed-identity/)

  - [ Azure Policy](../services/policy/)

  - [ Azure Resource Graph](../services/resourcegraph/)

  - [ Azure Management Groups](../services/managementgroups/)

  - [ ARM Template Deployments](../services/deployments/)

- [ Contributing](../contributing/)

- [ Changelog](../changelog/)

Table of contents

- [ 1: Start floci-az with TLS enabled](#1-start-floci-az-with-tls-enabled)

- [ 2: Trust the certificate](#2-trust-the-certificate)

- [ 3: Configure the provider](#3-configure-the-provider)

- [ Troubleshooting](#troubleshooting)

# Terraform / OpenTofu (azurerm provider)

You can run the `hashicorp/azurerm` provider against floci-az for local-first IaC: `plan` / `apply` / `destroy` against the emulator instead of real Azure.

> **TLS is required.** The azurerm provider discovers the cloud over **HTTPS** (`GET https://<host>/metadata/endpoints`). floci-az serves plain HTTP by default, so you **must** enable TLS or the provider fails before it sends a single resource request. This is the single most common setup mistake (see [Troubleshooting](#troubleshooting)).

---

## 1: Start floci-az with TLS enabled

```
# docker-compose.yml
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock   # for container-backed services (SQL, Postgres, …)
      - ./data:/app/data                            # persist the generated cert across restarts
    environment:
      FLOCI_AZ_TLS_ENABLED: "true"
```

floci-az serves HTTP and HTTPS on the **same** port (4577) via a protocol-sniffing proxy. A self-signed certificate is generated at startup and served at `GET /_floci/tls-cert`.

## 2: Trust the certificate

The provider validates the TLS chain, so the self-signed cert must be trusted by the machine running `tofu`/`terraform`:

```
curl -sf http://localhost:4577/_floci/tls-cert -o floci-az.crt
# Linux: copy into the system trust store, then refresh
sudo cp floci-az.crt /usr/local/share/ca-certificates/ && sudo update-ca-certificates
```

(Adjust for your OS trust store; on a CI runner you typically install it the same way.)

## 3: Configure the provider

```
terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 3.0"   # works with the 3.x and 4.x provider lines
    }
  }
}

provider "azurerm" {
  features {}
  use_cli = false

  environment   = "stack"            # use a custom cloud described by metadata_host
  metadata_host = "localhost:4577"   # floci-az serves /metadata/endpoints over HTTPS here

  subscription_id = "00000000-0000-0000-0000-000000000001"
  tenant_id       = "00000000-0000-0000-0000-000000000002"
  client_id       = "00000000-0000-0000-0000-000000000003"
  client_secret   = "fake-secret"    # credentials are not validated
}
```

No `skip_provider_registration` (3.x) or `resource_provider_registrations = "none"` (4.x) is needed: floci-az lists every namespace it serves as already registered, so the provider has nothing to register. See [Resource providers](../services/arm/#resource-providers).

Then the usual flow works:

```
tofu init
tofu apply
tofu destroy
```

See the `compatibility-tests/compat-opentofu` directory in the repo for a complete, CI-verified example (resource group, storage, Key Vault, VNet, VM, Redis, ACR, PostgreSQL Flexible Server).

---

## Troubleshooting

**`http: server gave HTTP response to HTTPS client`** (or `tls: first record does not look
like a TLS handshake`) when the provider configures itself:

```
Configuring cloud environment from Metadata Service at localhost:4577
```

→ TLS is not enabled. floci-az is answering the provider's HTTPS metadata probe with plain HTTP. **Fix:** set `FLOCI_AZ_TLS_ENABLED=true`, restart, and trust the cert (steps 1–2 above).

**`x509: certificate signed by unknown authority`**

→ TLS is enabled but the self-signed certificate is not trusted on the machine running the provider. Re-do step 2, or point `SSL_CERT_FILE` at the downloaded `floci-az.crt`.

**`GET /_floci/tls-cert` returns `"tlsEnabled": false`**

→ Confirms TLS is off. Enable it as in step 1\.

Made with [ Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)

---

## Assistant

_Thinking:_

Now I have everything. Present final plan in Spanish, concise. Include TLS requirement (key gotcha), cert trust on Windows, provider config, infra files.

Note: compat test for terraform covers resource group, storage, key vault, VNet, VM, Redis, ACR, PostgreSQL — Container Apps not explicitly in that list, but ARM support for Microsoft.App exists. I should be honest: Terraform + Container Apps (azurerm_container_app) should work through ARM but the CI-verified suite doesn't explicitly list container apps. Mention as risk.

One question maybe: does he already have Terraform installed / where to put files? Keep short. Maybe ask about structure: infra/ at root vs per-service. Actually plan mode — present plan, one question max. I could ask if the repo structure for microservices exists yet (it's empty) — but that's fine, plan covers scaffolding.

Let me write the final plan.

Perfecto, Terraform es la opción con **mejor evidencia** en floci-az: el repo tiene suite de compatibilidad CI verificada (`compat-opentofu`) con `hashicorp/azurerm` 3.x y 4.x corriendo `plan`/`apply`/`destroy` contra el emulador.

**Gotcha crítico que descubrí**: el provider `azurerm` descubre la nube vía HTTPS (`GET /metadata/endpoints`). floci-az sirve HTTP por defecto → **tenés que habilitar TLS** o el provider falla antes de mandar el primer request. Es el error más común de setup.

## Plan final de archivos

**1. `docker-compose.yml`** (raíz)
- `floci/floci-az:latest`
- `4577:4577` + `/var/run/docker.sock` (requerido para Container Apps modo real)
- `FLOCI_AZ_TLS_ENABLED: "true"` ← **imprescindible para Terraform**
- `FLOCI_AZ_SERVICES_CONTAINER_APPS_MOCKED: "false"` ← modo real
- `FLOCI_AZ_STORAGE_MODE: hybrid`
- Volumen `./data:/app/data` para persistir el cert TLS y los datos
- Healthcheck + red `app-net`

**2. `.env.example`** — connection string de `devstoreaccount1`, endpoints

**3. `infra/`**
- `versions.tf` — `required_providers` azurerm `>= 3.0`
- `providers.tf` — `environment = "stack"`, `metadata_host = "localhost:4577"`, `use_cli = false`, credenciales fake (no se validan)
- `main.tf` — resource group + `azurerm_container_app_environment` + un `azurerm_container_app` de ejemplo (ingress externo, puerto 80, `minReplicas: 1`)
- `variables.tf` / `outputs.tf` — output del FQDN

**4. `scripts/trust-cert.ps1`** — baja `http://localhost:4577/_floci/tls-cert` y lo instala en el trust store de Windows (o setea `SSL_CERT_FILE`)

**5. `README`** — flujo: `docker compose up -d` → correr script de cert → `terraform init && apply` → invocar por FQDN vía `Host: <app>.<env>.azurecontainerapps.io` en `localhost:4577`

## Riesgo a declarar

La suite CI de Terraform cubre resource group, storage, Key Vault, VNet, VM, Redis, ACR y PostgreSQL — **Container Apps (`azurerm_container_app`) no está explícitamente en esa lista**, aunque el endpoint ARM `Microsoft.App` está documentado y soportado. Funciona vía ARM, pero es el punto donde puede haber diferencias de esquema con el provider. Lo primero que haría al implementar es validar `terraform plan` con el environment + una app antes de escalar a todos los microservicios.

¿Arranco con este plan cuando cambies a modo de implementación?

---

## User

Mostrame como quedarian los archivos completos

---

## Assistant

_Thinking:_

User asks to show the complete files. Plan mode: I can show file contents in conversation (not write them). Let me draft the complete files.

Files:
1. docker-compose.yml
2. .env.example
3. infra/versions.tf
4. infra/providers.tf
5. infra/main.tf
6. infra/variables.tf
7. infra/outputs.tf
8. scripts/trust-cert.ps1
9. README section

Check azurerm container app resource schema: azurerm_container_app requires registry config for image, ingress block: external, target_port, transport. Container resources: cpu, memory. scale block min_replicas max_replicas. secrets block. env in template container: name, value, secret_name.

azurerm_container_app_environment: resource_group_name, location, log_analytics_workspace block (optional? In azurerm 3.x, log_analytics_workspace block optional since 3.x? Actually historically required, but became optional in v3.x — yes, `log_analytics_workspace` became optional in provider 3.x). To be safe, include TLS ingress config? azurerm_container_app_environment supports `marketplace` ... For floci, workloads profile? azurerm_container_app_environment requires `workload_profile` maybe optional. Keep simple.

Note: azurerm_container_app requires `container_app_environment_identity`? No.

Provider config: environment = "stack", metadata_host = "localhost:4577". Note TLS on same port with self-signed cert — the provider over HTTPS needs trusted cert; also `resource_provider_registrations` not needed.

Container App registry: images must be built locally so Docker daemon can see them (floci-az spawns containers via local docker socket). Image e.g. `latiendita-api:local`.

For the container app, scale min_replicas 1 to keep warm (scale-to-zero on demand).

Also transport: "http" for ingress.

Let me write out the files carefully.

providers.tf:

```hcl
provider "azurerm" {
  features {}

  use_cli = false

  environment   = "stack"
  metadata_host = "localhost:4577"

  subscription_id = "00000000-0000-0000-0000-000000000001"
  tenant_id       = "00000000-0000-0000-0000-000000000002"
  client_id       = "00000000-0000-0000-0000-000000000003"
  client_secret   = "fake-secret"
}
```

main.tf:

```hcl
resource "azurerm_resource_group" "this" {
  name     = "latiendita"
  location = "eastus"
}

resource "azurerm_container_app_environment" "this" {
  name                = "local"
  location = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
}

resource "azurerm_container_app" "pedidos" {
  name                         = "pedidos-api"
  ...
 template {
    container {
      name  = "pedidos"
      image = "latiendita/pedidos-api:local"
      cpu   = 0.5
      memory = "1Gi"
    }
    min_replicas = 1
    max_replicas = 2
  }
  ingress {
    external_enabled = true
    target_port      = 8080
    transport        = "http"
    traffic_weight {
      percentage = 100
      latest_revision = true
    }
  }
}
```

Note: azurerm_container_app `registry { server, image, username, password }` optional for local images (no auth) — local images are already on the daemon so no registry needed; but provider might require registry for private images? Not required.

Also `workload_profile_name` required if environment has profiles; none → fine.

Wait — azurerm 4.x may require `ingress.transport` values http/http2/auto. fine.

variables: location default "eastus", image default.

outputs: fqdn, and also note invocation.

docker-compose.yml:

```yaml
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - ./data:/app/data
    environment:
      FLOCI_AZ_TLS_ENABLED: "true"
      FLOCI_AZ_SERVICES_CONTAINER_APPS_MOCKED: "false"
      FLOCI_AZ_STORAGE_MODE: hybrid
    healthcheck:
      test: ["CMD", "curl", "-sf", "http://localhost:4577/_floci/health"]  # need to verify health endpoint
```

I don't know the health endpoint. Risky to invent. Better use `curl -sf http://localhost:4577/_floci/tls-cert` which is documented. But inside container curl may not exist (native image, distroless?). Hmm. Alternative: use `CMD-SHELL` with wget? Uncertain. I could mark healthcheck with a documented endpoint: `GET /_floci/tls-cert` returns `tlsEnabled`. Docs mentioned `GET /_floci/tls-cert` returns `{"tlsEnabled": false}` — good endpoint. But binary availability of curl in the image unknown. Safer: mention healthcheck optional and use TCP check? Common approach:

```
healthcheck:
  test: ["CMD-SHELL", "curl -sf http://localhost:4577/_floci/tls-cert || exit 1"]
```

I'll note uncertainty and that it can be removed if image lacks curl. Or check docker-compose docs page from floci for official healthcheck. Let me fetch https://floci.io/floci-az/configuration/docker-compose/ quickly to be accurate.

Antes de mostrarte los archivos, verifico el compose oficial para no inventar el healthcheck.

**Tool: webfetch**

**Input:**
```json
{
  "format": "markdown",
  "url": "https://floci.io/floci-az/configuration/docker-compose/"
}
```

[ Skip to content](#docker-compose-configuration)

[ ![logo](../../assets/floci-header.svg)](../.. "Floci-AZ")

Floci-AZ

Docker Compose

Initializing search

[](https://github.com/floci-io/floci-az "Go to repository")

floci-io/floci-az

- [ Home](../..)

- [ Getting Started](../../getting-started/quick-start/)

- [ Configuration](./)

- [ Web Console](../../ui/)

- [ Services](../../services/)

- [ Contributing](../../contributing/)

- [ Changelog](../../changelog/)

[ ![logo](../../assets/floci-header.svg)](../.. "Floci-AZ") Floci-AZ

[](https://github.com/floci-io/floci-az "Go to repository")

floci-io/floci-az

- [ Home](../..)

- Getting Started

  Getting Started

  - [ Quick Start](../../getting-started/quick-start/)

  - [ Installation](../../getting-started/installation/)

  - [ Azure CLI & SDK Setup](../../getting-started/azure-setup/)

  - [ Terraform / OpenTofu](../../terraform/)

- Configuration

  Configuration

  - Docker Compose [ Docker Compose](./)

    Table of contents

    - [ Common Scenarios](#common-scenarios)

      - [ Storage only (no Functions)](#storage-only-no-functions)

      - [ All services (default)](#all-services-default)

      - [ With persistent storage](#with-persistent-storage)

      - [ With Azure SQL Database](#with-azure-sql-database)

      - [ CI / Ephemeral: maximum speed](#ci-ephemeral-maximum-speed)

      - [ Selective services](#selective-services)

      - [ Per-service storage override](#per-service-storage-override)

      - [ With Docker-backed engines (Cosmos MongoDB, Event Hubs…)](#with-docker-backed-engines-cosmos-mongodb-event-hubs)

      - [ Sidecars and Docker networks](#sidecars-and-docker-networks)

      - [ With the Azure CLI inside the container](#with-the-azure-cli-inside-the-container)

      - [ Multi-container (your app \+ floci-az)](#multi-container-your-app-floci-az)

    - [ Environment Variable Reference](#environment-variable-reference)

      - [ Core](#core)

      - [ Storage](#storage)

      - [ Per-service storage overrides](#per-service-storage-overrides)

      - [ Enable / disable services](#enable-disable-services)

      - [ Azure SQL Database](#azure-sql-database)

      - [ Azure Functions](#azure-functions)

      - [ Docker daemon](#docker-daemon)

      - [ Web console](#web-console)

    - [ Resource Identity Labels](#resource-identity-labels)

    - [ Docker Socket Access](#docker-socket-access)

    - [ Health Check](#health-check)

  - [ Docker Images](../docker-images/)

  - [ Ports Reference](../ports/)

  - [ Emulator Endpoints](../endpoints/)

  - [ Storage Modes](../storage/)

  - Advanced

    Advanced

    - [ application.yml Reference](../advanced/application-yml/)

- Web Console

  Web Console

  - [ Overview](../../ui/)

  - [ Console Contract v2](../../ui/console-contract/)

- Services

  Services

  - [ Overview](../../services/)

  - [ Azure Resource Manager](../../services/arm/)

  - [ Blob Storage](../../services/blob/)

  - [ Queue Storage](../../services/queue/)

  - [ Table Storage](../../services/table/)

  - [ Azure Functions](../../services/functions/)

  - [ App Configuration](../../services/app-config/)

  - [ Azure SignalR Service](../../services/signalr/)

  - [ Key Vault](../../services/key-vault/)

  - [ Microsoft Entra ID](../../services/entra/)

  - [ Microsoft Graph](../../services/graph/)

  - [ Event Hubs](../../services/event-hub/)

  - [ Service Bus](../../services/service-bus/)

  - [ Cosmos DB](../../services/cosmos/)

  - [ Azure SQL Database](../../services/sql/)

  - [ Azure Database for PostgreSQL](../../services/postgresql/)

  - [ Azure Database for MySQL](../../services/mysql/)

  - [ Azure Database for MariaDB](../../services/mariadb/)

  - [ Azure Kubernetes Service](../../services/aks/)

  - [ Azure Container Apps](../../services/container-apps/)

  - [ API Management](../../services/apim/)

  - [ Virtual Network](../../services/network/)

  - [ Virtual Machines](../../services/vm/)

  - [ Azure Cache for Redis](../../services/redis/)

  - [ Azure Container Registry](../../services/acr/)

  - [ Azure Container Instances](../../services/aci/)

  - [ Event Grid](../../services/event-grid/)

  - [ Azure Monitor / Log Analytics](../../services/monitor/)

  - [ Communication Services Email](../../services/email/)

  - [ Managed Identity](../../services/managed-identity/)

  - [ Azure Policy](../../services/policy/)

  - [ Azure Resource Graph](../../services/resourcegraph/)

  - [ Azure Management Groups](../../services/managementgroups/)

  - [ ARM Template Deployments](../../services/deployments/)

- [ Contributing](../../contributing/)

- [ Changelog](../../changelog/)

Table of contents

- [ Common Scenarios](#common-scenarios)

  - [ Storage only (no Functions)](#storage-only-no-functions)

  - [ All services (default)](#all-services-default)

  - [ With persistent storage](#with-persistent-storage)

  - [ With Azure SQL Database](#with-azure-sql-database)

  - [ CI / Ephemeral: maximum speed](#ci-ephemeral-maximum-speed)

  - [ Selective services](#selective-services)

  - [ Per-service storage override](#per-service-storage-override)

  - [ With Docker-backed engines (Cosmos MongoDB, Event Hubs…)](#with-docker-backed-engines-cosmos-mongodb-event-hubs)

  - [ Sidecars and Docker networks](#sidecars-and-docker-networks)

  - [ With the Azure CLI inside the container](#with-the-azure-cli-inside-the-container)

  - [ Multi-container (your app \+ floci-az)](#multi-container-your-app-floci-az)

- [ Environment Variable Reference](#environment-variable-reference)

  - [ Core](#core)

  - [ Storage](#storage)

  - [ Per-service storage overrides](#per-service-storage-overrides)

  - [ Enable / disable services](#enable-disable-services)

  - [ Azure SQL Database](#azure-sql-database)

  - [ Azure Functions](#azure-functions)

  - [ Docker daemon](#docker-daemon)

  - [ Web console](#web-console)

- [ Resource Identity Labels](#resource-identity-labels)

- [ Docker Socket Access](#docker-socket-access)

- [ Health Check](#health-check)

# Docker Compose Configuration

Most users configure floci-az entirely through environment variables: no config files needed. Every `floci-az.*` setting maps to a `FLOCI_AZ_*` env var (replace `.` with `_`, uppercase).

---

## Common Scenarios

### Storage only (no Functions)

The simplest setup: skips the Docker socket mount entirely:

```
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    environment:
      FLOCI_AZ_SERVICES_FUNCTIONS_ENABLED: "false"
```

### All services (default)

```
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock  # required for Azure Functions
```

### With persistent storage

Data survives container restarts. Mount a local directory and set a storage mode:

```
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    volumes:
      - ./data:/app/data
      - /var/run/docker.sock:/var/run/docker.sock
    environment:
      FLOCI_AZ_STORAGE_MODE: hybrid        # in-memory + async flush every 5 s (recommended)
      # FLOCI_AZ_STORAGE_MODE: wal         # every write goes to disk before responding
      # FLOCI_AZ_STORAGE_MODE: persistent  # flush only on graceful shutdown
```

### With Azure SQL Database

SQL defaults to control-plane-only mode and needs no Docker socket. To opt into managed SQL Server containers, set the provider to `managed`, accept the EULA, and mount the Docker socket:

```
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    environment:
      FLOCI_AZ_SERVICES_SQL_DATA_PLANE_PROVIDER: managed
      FLOCI_AZ_SERVICES_SQL_ACCEPT_EULA: "Y"   # accept the Microsoft SQL Server EULA
      # FLOCI_AZ_SERVICES_SQL_IMAGE: "mcr.microsoft.com/mssql/server:2025-latest"
```

> SQL Server containers bind a random host port directly via Docker: do **not** add those ports to the `floci-az` service's `ports:` block. Use the `/connect` endpoint to discover the port.

### CI / Ephemeral: maximum speed

Pure in-memory, no socket required, fastest startup:

```
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    environment:
      FLOCI_AZ_STORAGE_MODE: memory
      FLOCI_AZ_SERVICES_FUNCTIONS_ENABLED: "false"
```

### Selective services

Disable services you don't use:

```
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    environment:
      FLOCI_AZ_SERVICES_BLOB_ENABLED: "true"
      FLOCI_AZ_SERVICES_QUEUE_ENABLED: "true"
      FLOCI_AZ_SERVICES_TABLE_ENABLED: "false"
      FLOCI_AZ_SERVICES_FUNCTIONS_ENABLED: "false"
      FLOCI_AZ_SERVICES_APP_CONFIG_ENABLED: "false"
```

### Per-service storage override

Run most services in-memory, but use WAL for blob durability:

```
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    volumes:
      - ./data:/app/data
    environment:
      FLOCI_AZ_STORAGE_MODE: memory
      FLOCI_AZ_STORAGE_SERVICES_BLOB_MODE: wal
```

### With Docker-backed engines (Cosmos MongoDB, Event Hubs…)

Docker-backed engines (Cosmos MongoDB/PostgreSQL/Cassandra/Gremlin) and Event Hubs sidecars (Artemis, Redpanda) are launched as **sibling containers** by floci-az via the Docker socket. They bind their ports directly on the host; **do not** publish those ports on the `floci-az` service:

```
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
      - "4578:4578"   # Cosmos DB: Java SDK (HTTPS)
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    environment:
      # Cosmos engines
      FLOCI_AZ_SERVICES_COSMOS_ENGINES_MONGODB_ENABLED: "true"
      FLOCI_AZ_SERVICES_COSMOS_ENGINES_POSTGRESQL_ENABLED: "true"
      # Event Hubs
      FLOCI_AZ_SERVICES_EVENT_HUB_ENABLED: "true"
```

Once the sidecars start, their ports are available on the host: `localhost:27017` (MongoDB), `localhost:5432` (PostgreSQL), `localhost:5672` (AMQP / Artemis).

### Sidecars and Docker networks

Docker Compose puts its services on a user-defined network, where containers resolve each other by name. A plain `docker run` without `--network` puts floci-az on Docker's default `bridge` network instead, which does not resolve container names. floci-az still reaches its sidecars there by container IP, so Redis caches, container registries and database servers finish provisioning, but the host names it reports for Redis, PostgreSQL, MySQL, MariaDB and SQL Server are container names that clients on the default bridge cannot resolve. floci-az logs a warning at startup in that case. Use a user-defined network:

```
docker network create floci-net
docker run -d --name floci-az --network floci-net -p 4577:4577 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -e FLOCI_AZ_SERVICES_DOCKER_NETWORK=floci-net \
  floci/floci-az:latest
```

### With the Azure CLI inside the container

Use the `latest-compat` image when your scripts need Azure tooling inside the container. It includes the Azure CLI and `azfloci`. Call `azfloci` to target the local emulator without passing a connection string; plain `az` still needs explicit connection settings. See [Docker Images](../docker-images/) for what the compat image contains.

```
services:
  floci-az:
    image: floci/floci-az:latest-compat
    ports:
      - "4577:4577"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
```

### Multi-container (your app \+ floci-az)

When your application also runs in Docker, use the service name as the hostname:

```
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    networks:
      - app-net

  my-app:
    image: my-app:latest
    environment:
      AZURE_STORAGE_CONNECTION_STRING: >-
        DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;
        AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;
        BlobEndpoint=http://floci-az:4577/devstoreaccount1;
        QueueEndpoint=http://floci-az:4577/devstoreaccount1-queue;
        TableEndpoint=http://floci-az:4577/devstoreaccount1-table;
      # App Configuration: https:// required by the SDK; use ForceHttp transport in your client
      AZURE_APPCONFIG_ENDPOINT: https://floci-az:4577/devstoreaccount1-appconfig
    depends_on:
      - floci-az
    networks:
      - app-net

networks:
  app-net:
```

---

## Environment Variable Reference

All variables are optional; the default applies when unset.

### Core

| Variable | Default | Description |
| --- | --- | --- |
| `FLOCI_AZ_PORT` | `4577` | Port the emulator listens on |
| `FLOCI_AZ_BASE_URL` | `http://localhost:4577` | Base URL embedded in API responses |
| `FLOCI_AZ_HOSTNAME` | *(unset)* | Override the hostname in SAS and invoke URLs (useful behind a reverse proxy) |
| `FLOCI_AZ_AUTH_MODE` | `dev` | Not enforced yet: every value, `strict` included, accepts any credentials without validating SharedKey signatures. Blob service SAS signatures are checked against `auth.storage-account-keys` regardless |
| `FLOCI_AZ_PARITY` | `compat` | Azure parity level. `compat`: responses stay as floci-az has always answered them. `azure`: every parity override on. `strict`: `azure` plus Azure's refusals. See the [ARM page](../../services/arm/#parity-levels) |
| `FLOCI_AZ_PARITY_OVERRIDES_SUBSCRIPTION_LISTING` | *(on from `azure`)* | `true`: `GET /subscriptions` also lists subscriptions registered on first use, and a subscription is echoed with the directory's id |
| `FLOCI_AZ_PARITY_OVERRIDES_STRICT_SUBSCRIPTIONS` | *(on from `strict`)* | `true`: an unknown subscription is `404 SubscriptionNotFound`; `false`: it is registered on first use |
| `FLOCI_AZ_PARITY_OVERRIDES_RESOURCE_GROUP_SEMANTICS` | *(on from `azure`)* | `true`: a resource-group re-PUT answers `200`, a resource PUT under a missing group creates the group, and groups persist in the directory store |
| `FLOCI_AZ_PARITY_OVERRIDES_RESOURCE_GROUP_DELETE` | *(on from `azure`)* | `true`: a resource-group `DELETE` deletes every resource in the group and answers `202` with a `Location` to poll; `false`: `200`, only the group is removed |
| `FLOCI_AZ_PARITY_OVERRIDES_STRICT_RESOURCE_GROUPS` | *(on from `strict`)* | `true`: a request for a resource under a group that does not exist is `404 ResourceGroupNotFound`, and groups persist in the directory store |
| `FLOCI_AZ_CLOUD_DEFAULT` | `AzureCloud` | Cloud of the default tenant: `AzureCloud`, `AzureUSGovernment` or `AzureChinaCloud` |

### Storage

| Variable | Default | Description |
| --- | --- | --- |
| `FLOCI_AZ_STORAGE_MODE` | `memory` | Global storage backend: `memory` · `persistent` · `hybrid` · `wal` |
| `FLOCI_AZ_STORAGE_PERSISTENT_PATH` | `/app/data` | Container-side directory for persisted state |
| `FLOCI_AZ_STORAGE_HOST_PERSISTENT_PATH` | *(same as above)* | Host-side path when running Docker-in-Docker |
| `FLOCI_AZ_STORAGE_WAL_COMPACTION_INTERVAL_MS` | `30000` | How often the WAL is compacted (ms) |
| `FLOCI_AZ_STORAGE_HYBRID_FLUSH_INTERVAL_MS` | `5000` | How often hybrid mode flushes to disk (ms) |

### Per-service storage overrides

| Variable | Default | Description |
| --- | --- | --- |
| `FLOCI_AZ_STORAGE_SERVICES_BLOB_MODE` | *(global)* | Storage mode for Blob Storage only |
| `FLOCI_AZ_STORAGE_SERVICES_QUEUE_MODE` | *(global)* | Storage mode for Queue Storage only |
| `FLOCI_AZ_STORAGE_SERVICES_TABLE_MODE` | *(global)* | Storage mode for Table Storage only |
| `FLOCI_AZ_STORAGE_SERVICES_APP_CONFIG_MODE` | *(global)* | Storage mode for App Configuration only |
| `FLOCI_AZ_STORAGE_SERVICES_DIRECTORY_MODE` | *(global)* | Storage mode for the ARM tenant and subscription directory only |

### Enable / disable services

| Variable | Default | Description |
| --- | --- | --- |
| `FLOCI_AZ_SERVICES_BLOB_ENABLED` | `true` | Enable or disable Blob Storage |
| `FLOCI_AZ_SERVICES_QUEUE_ENABLED` | `true` | Enable or disable Queue Storage |
| `FLOCI_AZ_SERVICES_TABLE_ENABLED` | `true` | Enable or disable Table Storage |
| `FLOCI_AZ_SERVICES_FUNCTIONS_ENABLED` | `true` | Enable or disable Azure Functions |
| `FLOCI_AZ_SERVICES_APP_CONFIG_ENABLED` | `true` | Enable or disable App Configuration |
| `FLOCI_AZ_SERVICES_COSMOS_ENABLED` | `true` | Enable or disable Cosmos DB (SQL API) |
| `FLOCI_AZ_SERVICES_KEY_VAULT_ENABLED` | `true` | Enable or disable Key Vault |
| `FLOCI_AZ_SERVICES_EVENT_HUB_ENABLED` | `true` | Enable or disable Event Hubs |
| `FLOCI_AZ_SERVICES_SQL_ENABLED` | `true` | Enable or disable Azure SQL Database |

### Azure SQL Database

| Variable | Default | Description |
| --- | --- | --- |
| `FLOCI_AZ_SERVICES_SQL_DATA_PLANE_PROVIDER` | `none` | `none` for ARM state only; `managed` for a real SQL Server container; `external` is reserved |
| `FLOCI_AZ_SERVICES_SQL_MOCKED` | *(unset)* | Deprecated provider alias: `true` = `none`, `false` = `managed` |
| `FLOCI_AZ_SERVICES_SQL_ACCEPT_EULA` | `N` | Set to `Y` to accept the Microsoft SQL Server EULA (managed mode only) |
| `FLOCI_AZ_SERVICES_SQL_IMAGE` | `mcr.microsoft.com/mssql/server:2025-latest` | Docker image for SQL Server containers |
| `FLOCI_AZ_SERVICES_SQL_STARTUP_TIMEOUT_SECONDS` | `60` | Seconds to wait for SQL Server to become ready |

### Azure Functions

| Variable | Default | Description |
| --- | --- | --- |
| `FLOCI_AZ_SERVICES_FUNCTIONS_EPHEMERAL` | `false` | `true`: fresh container per invocation; `false`: reuse warm containers |
| `FLOCI_AZ_SERVICES_FUNCTIONS_CONTAINER_IDLE_TIMEOUT_SECONDS` | `300` | Evict warm containers idle longer than this; `0` disables eviction |
| `FLOCI_AZ_SERVICES_FUNCTIONS_CODE_PATH` | `~/.floci-az/functions` | Where extracted function code is stored on the host |
| `FLOCI_AZ_SERVICES_FUNCTIONS_DOCKER_HOST_OVERRIDE` | *(unset)* | Override the hostname function containers use to reach floci-az |

### Docker daemon

| Variable | Default | Description |
| --- | --- | --- |
| `FLOCI_AZ_DOCKER_DOCKER_HOST` | `unix:///var/run/docker.sock` | Docker daemon socket, unix socket or `tcp://host:port` |
| `FLOCI_AZ_DOCKER_ENDPOINT_MODE` | `auto` | How sidecar containers are addressed, by floci-az and in the host/port reported to clients. `auto`: container name/IP and internal port when floci-az runs in a container, `localhost` and the published port otherwise. `published`: always the Docker daemon's host (from `docker-host`) and the published port. Use it for remote daemons such as docker-in-docker or kubedock, whose containers aren't directly reachable |
| `FLOCI_AZ_SERVICES_DOCKER_NETWORK` | *(detected)* | Docker network sidecar containers join. Defaults to the network floci-az itself runs on, preferring a user-defined one. On the default `bridge` network floci-az reaches sidecars by container IP |
| `FLOCI_AZ_DOCKER_LOG_MAX_SIZE` | `10m` | Max log file size per function container |
| `FLOCI_AZ_DOCKER_LOG_MAX_FILE` | `3` | Max rotated log files per function container |
| `FLOCI_AZ_DOCKER_DOCKER_CONFIG_PATH` | *(unset)* | Path to Docker `config.json` for private registry auth |
| `FLOCI_AZ_DOCKER_RESOURCE_NAMESPACE` | *(unset)* | Namespace inserted into the name of every container and volume floci-az creates (`floci-az-<ns>-...`) and added as the `floci_namespace` label, so parallel emulator instances on one Docker host don't collide |

### Web console

| Variable | Default | Description |
| --- | --- | --- |
| `FLOCI_AZ_UI_ENABLED` | `true` | Allow `/_floci/ui` to start the [web console](../../ui/) |
| `FLOCI_AZ_UI_IMAGE` | `floci/floci-ui:0.7.0` | Console image |
| `FLOCI_AZ_UI_PORT` | `4500` | Host port the console is published on. No `ports:` entry needed |
| `FLOCI_AZ_UI_ENDPOINT` | *(derived)* | URL the console uses to reach floci-az |

---

## Resource Identity Labels

Every container and volume floci-az creates carries the base labels `floci=true`, `floci_emulator=floci-az` and, when `FLOCI_AZ_DOCKER_RESOURCE_NAMESPACE` is set, `floci_namespace`. A container backing an emulated Azure resource also carries labels tying it back to that resource, additive to the base labels:

| Label | Value | Purpose |
| --- | --- | --- |
| `io.floci` | `az` | Cloud provider, for multi-cloud discovery when several Floci emulators share a host |
| `io.floci.service` | e.g. `postgres` | The Azure service the container backs |
| `io.floci.resource-id` | e.g. `orders-db` | The Azure resource name (server, cache, cluster, VM, container group, container app, namespace, function app) |
| `io.floci.subscription` | the subscription id | The subscription the resource belongs to |
| `io.floci.resource-group` | the resource group name | The resource group the resource belongs to |
| `io.floci.location` | e.g. `eastus` | The Azure location of the resource |
| `floci_service` | same as `io.floci.service` | Legacy: still written, prefer the new key |

This makes `docker ps --filter label=io.floci.service=postgres --filter label=io.floci.resource-id=orders-db` resolve an emulated resource to its backing container directly. A label whose value is unknown is omitted rather than written empty.

Labels are written when a container is created, so they apply to containers floci-az starts after upgrading. A container that already existed keeps the labels it was created with (typically only the base labels and `floci_service`) until floci-az recreates it, for example when the resource is deleted and created again.

- **All six keys:** Azure Cache for Redis, Azure Database for PostgreSQL, MySQL and MariaDB, Azure SQL Database, AKS, Virtual Machines, Container Instances and Container Apps.

- **No scope keys** (`io.floci.subscription`, `io.floci.resource-group`, `io.floci.location`): Service Bus and Event Hubs namespaces and Azure Functions apps, whose data-plane APIs carry no Azure scope.

- **No `io.floci.resource-id` and no scope keys:** the shared ACR registry, the Event Hubs Kafka (Redpanda) sidecar and the per-API Cosmos DB engine containers, which are singletons with no single resource behind them.

`io.floci`, `io.floci.service` and `io.floci.resource-id` follow the keys floci-aws uses, so a host running both emulators can filter their containers the same way; each emulator adds its own scope keys (`io.floci.account` and `io.floci.region` there, the subscription, resource group and location keys here). floci-gcp and floci-oci do not write these keys yet.

---

## Docker Socket Access

The Docker socket mount (`/var/run/docker.sock`) is required for Azure Functions. The container entrypoint automatically detects the socket's group ID at runtime and adjusts permissions: this works on both Docker Desktop (macOS/Windows) and native Linux Docker with no manual configuration.

If you don't need Functions, omit the socket mount and set `FLOCI_AZ_SERVICES_FUNCTIONS_ENABLED=false`.

---

## Health Check

The published image ships its own probe, `/usr/local/bin/healthcheck.sh`, and declares it as the image `HEALTHCHECK`, so `depends_on` with `condition: service_healthy` works without a `healthcheck:` block. The image has neither `curl` nor `wget`, so an override that calls either never passes. To tune the timings, point the override at the same script:

```
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    healthcheck:
      test: ["CMD", "healthcheck.sh"]
      interval: 5s
      timeout: 3s
      retries: 10

  my-app:
    depends_on:
      floci-az:
        condition: service_healthy
```

The script sends `GET /_floci/health` and passes on a `200`. The endpoint answers:

```
{"status": "UP", "version": "0.14.0", "edition": "floci-az-always-free"}
```

| Field | Meaning |
| --- | --- |
| `status` | `UP`, or `DOWN` with a `503` and a `problems` map when a service reports a problem |
| `version` | The floci-az release the image was built from (`FLOCI_AZ_VERSION`), or `dev` for a local build |
| `edition` | Always `floci-az-always-free` |

`GET /health` returns the same body. See [Emulator Endpoints](../endpoints/) for the other control-plane paths, including `POST /_admin/reset`.

`/ready` is the readiness probe: it answers `503` with `{"status":"DOWN"}` until every service has finished its startup work (for example Service Bus `start-on-boot` or eager Cosmos engines) and again once shutdown begins. In between it answers what `/health` (also served at `/_floci/health`) answers: `200` with `{"status":"UP"}`, or `503` when a service reports a problem. `/health` does not wait for startup to finish.

Made with [ Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)

---

## Assistant

_Thinking:_

Now I have everything. Build complete file contents.

Files:

1. docker-compose.yml — with healthcheck (image has no curl/wget; healthcheck.sh is the official script), TLS enabled, container apps mocked=false, hybrid storage, docker.sock, ./data volume.

Note: mock env var name: `FLOCI_AZ_SERVICES_CONTAINER_APPS_MOCKED: "false"`.

Also `FLOCI_AZ_SERVICES_DOCKER_NETWORK` — container apps real mode spawns containers; they join floci-az's network. Fine, default detected. But wait: floci-az runs in compose with a user-defined network; the container app replicas join that network. Ingress goes through floci-az:4577, good.

2. .env.example — connection string (localhost for host-run apps; note floci-az for containers). Since microservices are deployed as container apps in the emulator, they access storage via... Container app containers spawned by floci-az join the docker network, so they can use `http://floci-az:4577/...`. But they'd need the docker network name. FLOCI_AZ_BASE_URL default is http://localhost:4577 — inside container app containers, localhost is their own netns. Hmm — there's `FLOCI_AZ_SERVICES_FUNCTIONS_DOCKER_HOST_OVERRIDE` for functions. For container apps, the doc says sidecars share floci-az's network? Not stated. I shouldn't over-claim; note that storage endpoints inside containers should use the emulator's reachable host — I'll mention `FLOCI_AZ_HOSTNAME`/`FLOCI_AZ_BASE_URL` and flag verification.

Better: keep .env.example for host-side usage (terraform, local dev), and in README note the container-internal endpoint caveat as a verification step.

3. infra/versions.tf
4. infra/providers.tf
5. infra/variables.tf
6. infra/main.tf
7. infra/outputs.tf
8. scripts/trust-cert.ps1
9. README section (markdown block)

azurerm_container_app schema details:
- required: name, resource_group_name, location, template- environment_id
- ingress block: external_enabled, target_port, transport, traffic_weight (percentage, revision_name/latest_revision)
- registry block optional: server, username, password
- secret block: name, value
- template: container (name, image, cpu, memory, env, ports), min_replicas, max_replicas, volume, scale
- container env: name, value, secret_name
- container ports: name, container_port, protocol

azurerm_container_app_environment: name, resource_group_name, location. log_analytics block optional (since 3.x). Good.

For image: local image `latiendita/pedidos-api:local` built with docker build; no registry needed since it's on the local daemon. azurerm provider may not require registry.

TLS trust on Windows: PowerShell script — download cert, install to CurrentUser Root store:

```powershell
$ProgressPreference = 'SilentlyContinue'
$cert = Join-Path $PSScriptRoot "floci-az.crt"
Invoke-WebRequest -Uri "http://localhost:4577/_floci/tls-cert" -OutFile $cert
Import-Certificate -FilePath $cert -CertStoreLocation Cert:\CurrentUser\Root
```

Or set SSL_CERT_FILE env var for Terraform (Windows doesn't use it natively... actually Go's crypto/x509 on Windows respects SSL_CERT_FILE? Go on Windows uses the Windows certificate store, and SSL_CERT_FILE is not used... Actually Go's crypto/x509 on Windows does read the Windows store; SSL_CERT_FILE works on Unix. On Windows, Go respects `SSL_CERT_FILE`? Checking: Go source crypto/x509/root_windows.go uses CertOpenSystemStore — SSL_CERT_FILE not used. But newer Go versions? Not sure. Safer on Windows: install to CurrentUser\Root via Import-Certificate (may prompt admin? CurrentUser\Root doesn't need admin; LocalMachine does).

So script uses CurrentUser\Root.

Also note Windows Docker Desktop: /var/run/docker.sock mount works.

Terraform: `environment = "stack"` + `metadata_host = "localhost:4577"`.

One caveat: azurerm provider with environment "stack" requires metadata_host. Yes.

Also for Container Apps in real mode: floci-az spawns containers from image. The image must exist in local Docker daemon. Note in README.

Also `transport = "http"` in ingress. And `workload_profile_name` not needed.

Let me also add scale block: min_replicas=1, max_replicas=2.

azurerm_container_app `revision_mode` = "Single" required.

Output fqdn: `azurerm_container_app.pedidos.ingress[0].fqdn`.

Invocation: 
```
$FQDN = (terraform -chdir=infra output -raw pedidos_fqdn)
curl -H "Host: $FQDN" http://localhost:4577/
```

Wait — with TLS enabled, port 4577 serves HTTP+HTTPS via protocol sniffing. HTTP still works. Good.

Keep the response reasonably compact but complete. Files in code blocks with paths as headers.

One question: should I ask about which microservices? Not necessary. Show files; ask one question at the end maybe: confirm whether pedidos-api example is fine / service names. Actually the plan mentioned example. Keep.

Note the memory save protocol — I should save a memory (terraform decision). Do it before final message. Use mem_save with session_id — authoritative session ID is bound by adapter; schema says optional, "omit by default". I'll call mem_save with title/type/content/topic_key.

Let me do that save, then compose the final answer.

Verificado el detalle oficial del healthcheck (la imagen no trae curl/wget, usa `healthcheck.sh`). Guardo la decisión en memoria y te muestro todo.

**Tool: execute**

**Input:**
```json
{
  "code": "const r = await tools.engram.mem_save({\n  title: \"floci-az + Terraform para LaTiendita\",\n  type: \"decision\",\n  topic_key: \"infra/floci-az-emulator\",\n  content: \"**What**: Decided to use floci-az (local Azure emulator, MIT) with Terraform azurerm provider to deploy microservices as Azure Container Apps for LaTiendita.\\n**Why**: User wants to emulate Azure locally without cloud account; chose Terraform after tradeoffs discussion.\\n**Where**: LaTiendita repo — planned files: docker-compose.yml, .env.example, infra/*.tf, scripts/trust-cert.ps1\\n**Learned**: (1) floci-az needs FLOCI_AZ_TLS_ENABLED=true for azurerm provider (HTTPS metadata discovery) + trusting self-signed cert; (2) Container Apps real mode requires FLOCI_AZ_SERVICES_CONTAINER_APPS_MOCKED=false + docker.sock; (3) official healthcheck is /usr/local/bin/healthcheck.sh (image has no curl/wget); (4) terraform CI suite does not explicitly cover azurerm_container_app — validate plan first.\"\n});\nreturn r;"
}
```

{
  "id": 13,
  "judgment_required": false,
  "project": "latiendita",
  "project_path": "c:\\users\\damian\\repositorios\\latiendita",
  "project_source": "session",
  "result": "Memory saved: \"floci-az + Terraform para LaTiendita\" (decision)\n⚠ WARNING: Content contains an absolute filesystem path that may not be portable across machines. Use a repository-relative path instead.",
  "review_after": "2027-05-02 02:46:52",
  "state": "active",
  "sync_id": "obs-8ae27513adf5e966",
  "truncation": {
    "original_bytes": 798,
    "limit_bytes": 50000,
    "truncated": false
  }
}

---

## Assistant

_Thinking:_

Now present all files completely.

Acá están los archivos completos. Todo está verificado contra la documentación oficial de floci-az (compose, Container Apps y Terraform).

## 1. `docker-compose.yml`

```yaml
services:
  floci-az:
    image: floci/floci-az:latest
    ports:
      - "4577:4577"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock   # requerido: Container Apps modo real
      - ./data:/app/data                            # persiste datos + cert TLS
    environment:
      FLOCI_AZ_TLS_ENABLED: "true"                          # OBLIGATORIO para Terraform (azurerm descubre por HTTPS)
      FLOCI_AZ_SERVICES_CONTAINER_APPS_MOCKED: "false"      # modo real: levanta réplicas como contenedores Docker
      FLOCI_AZ_STORAGE_MODE: hybrid                         # in-memory + flush a disco cada 5s
    healthcheck:
      test: ["CMD", "healthcheck.sh"]   # la imagen NO trae curl/wget; este es el probe oficial
      interval: 5s
      timeout: 3s
      retries: 10
```

## 2. `.env.example`

```bash
# ── floci-az ──────────────────────────────────────────────
FLOCI_AZ_URL=http://localhost:4577

# Connection string estándar de devstoreaccount1 (cuenta y clave no se validan en dev)
AZURE_STORAGE_CONNECTION_STRING="DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:4577/devstoreaccount1;QueueEndpoint=http://localhost:4577/devstoreaccount1-queue;TableEndpoint=http://localhost:4577/devstoreaccount1-table;"

# ── Terraform ─────────────────────────────────────────────
TF_VAR_location="eastus"
TF_VAR_image_pedidos="latiendita/pedidos-api:local"
```

## 3. `infra/versions.tf`

```hcl
terraform {
  required_version = ">= 1.5.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 3.0"   # compatible con las líneas 3.x y 4.x
    }
  }
}
```

## 4. `infra/providers.tf`

```hcl
provider "azurerm" {
  features {}

  use_cli = false

  # Cloud personal apuntando a floci-az: el provider descubre la nube por
  # HTTPS en /metadata/endpoints — por eso TLS está obligatorio en el compose
  environment   = "stack"
  metadata_host = "localhost:4577"

  # Credenciales fake: floci-az no las valida
  subscription_id = "00000000-0000-0000-0000-000000000001"
  tenant_id       = "00000000-0000-0000-0000-000000000002"
  client_id       = "00000000-0000-0000-0000-000000000003"
  client_secret   = "fake-secret"
}
```

## 5. `infra/variables.tf`

```hcl
variable "location" {
  description = "Azure location (floci-az lo acepta cualquiera)"
  type        = string
  default     = "eastus"
}

variable "image_pedidos" {
  description = "Imagen Docker local del microservicio de pedidos (debe existir en el daemon local)"
  type        = string
  default     = "latiendita/pedidos-api:local"
}
```

## 6. `infra/main.tf`

```hcl
resource "azurerm_resource_group" "this" {
  name     = "latiendita"
  location = var.location
}

# ── Managed Environment (container apps) ──────────────────
resource "azurerm_container_app_environment" "this" {
  name                = "latiendita-env"
  location            = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
}

# ── Microservicio: pedidos ────────────────────────────────
resource "azurerm_container_app" "pedidos" {
  name                         = "pedidos-api"
  resource_group_name          = azurerm_resource_group.this.name
  location                     = azurerm_resource_group.this.location
  container_app_environment_id = azurerm_container_app_environment.this.id
  revision_mode                = "Single"

  template {
    container {
      name  = "pedidos"
      image = var.image_pedidos
      cpu   = 0.5
      memory = "1Gi"

      ports {
        name          = "http"
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
```

## 7. `infra/outputs.tf`

```hcl
output "resource_group" {
  value = azurerm_resource_group.this.name
}

output "pedidos_fqdn" {
  description = "FQDN asignado por floci-az al container app"
  value       = azurerm_container_app.pedidos.ingress[0].fqdn
}

output "invoke" {
  description = "Cómo invocar el servicio a través del emulador"
  value       = "curl -H \"Host: ${azurerm_container_app.pedidos.ingress[0].fqdn}\" http://localhost:4577/"
}
```

## 8. `scripts/trust-cert.ps1`

```powershell
# Baja el certificado auto-firmado de floci-az y lo instala en el trust store
# del usuario actual (no pide admin). Requisito: FLOCI_AZ_TLS_ENABLED=true
$ErrorActionPreference = "Stop"
$ProgressPreference    = "SilentlyContinue"

$out = Join-Path $PSScriptRoot "floci-az.crt"
Invoke-WebRequest -Uri "http://localhost:4577/_floci/tls-cert" -OutFile $out
Import-Certificate -FilePath $out -CertStoreLocation Cert:\CurrentUser\Root | Out-Null

Write-Host "Certificado floci-az instalado en Cert:\CurrentUser\Root"
```

## 9. Sección para el `README`

````markdown
## Azure local con floci-az + Terraform

### 1. Levantar el emulador
```powershell
docker compose up -d
docker compose ps   # esperar STATUS: healthy
```

### 2. Confiar el certificado TLS (una vez por máquina)
```powershell
.\scripts\trust-cert.ps1
```
> Sin esto, `terraform` falla con `x509: certificate signed by unknown authority`.

### 3. Desplegar los microservicios
```powershell
# La imagen debe existir en el daemon local: floci-az la levanta como contenedor real
docker build -t latiendita/pedidos-api:local ./services/pedidos

cd infra
terraform init
terraform apply
```

### 4. Invocar
```powershell
$FQDN = terraform -chdir=infra output -raw pedidos_fqdn
curl -H "Host: $FQDN" http://localhost:4577/
```

### 5. Verificar en la consola web
Abrir `http://localhost:4577/_floci/ui`

### Estructura
```
LaTiendita/
├── docker-compose.yml
├── .env.example
├── infra/            # Terraform (azurerm → floci-az)
├── scripts/trust-cert.ps1
└── services/pedidos/ # microservicio con su Dockerfile
```
````

## Notas importantes

1. **TLS es obligatorio** — el provider `azurerm` descubre la nube por HTTPS. Sin `FLOCI_AZ_TLS_ENABLED=true` ni el cert confiado, `terraform init` falla antes de tocar ningún recurso.
2. **`azurerm_container_app` no está en la suite CI de Terraform de floci** — el endpoint ARM `Microsoft.App` está documentado y soportado, pero el primer paso de implementación real debe ser validar `terraform plan`/`apply` con este environment + un app antes de replicar el patrón a todos los microservicios.
3. **Endpoint interno**: usé `floci-az:4577` en el env de la app porque los contenedores que floci-az levanta comparten su red Docker. Si al probar el servicio no conecta al storage, es el punto a verificar (documentación de Container Apps no especifica la red de las réplicas).

¿Arranco con la implementación cuando salgas de Plan mode?

---

## User

Que funcion cumple ### 2. Confiar el certificado TLS (una vez por máquina)
```powershell
.\scripts\trust-cert.ps1  y ### 4. Invocar
```powershell
$FQDN = terraform -chdir=infra output -raw pedidos_fqdn
curl -H "Host: $FQDN" http://localhost:4577/
``` ?

---

## Assistant

_Thinking:_

User asks what those two steps do. Explain clearly in Spanish, concise.

## Paso 2 — Confiar el certificado TLS

floci-az genera un **certificado auto-firmado** al arrancar (porque habilitamos TLS con `FLOCI_AZ_TLS_ENABLED=true`). Un certificado auto-firmado, por definición, no lo firma ninguna autoridad que tu sistema conozca.

¿Y por qué necesitás TLS si todo corre en local? Porque el provider `azurerm` de Terraform **descubre la nube a través de HTTPS** (`GET https://localhost:4577/metadata/endpoints`) antes de enviar el primer request. Si no confiás en el certificado, Terraform se detiene con `x509: certificate signed by unknown authority` sin llegar a crear nada.

El script hace exactamente tres cosas:

```powershell
Invoke-WebRequest ... -OutFile $out          # 1. baja el cert desde floci-az
Import-Certificate ... Cert:\CurrentUser\Root # 2. lo agrega a tu confianza de usuario
```

- `CurrentUser\Root` en vez de `LocalMachine\Root` → **no pide permisos de administrador**
- Se hace **una vez por máquina**, no por ejecución de Terraform
- Sin esto, el flujo `terraform init/apply` simplemente no funciona

## Paso 4 — Invocar

```powershell
$FQDN = terraform -chdir=infra output -raw pedidos_fqdn
curl -H "Host: $FQDN" http://localhost:4577/
```

Aquí hay dos conceptos en una sola línea:

**1. `terraform output`** — Terraform guarda en su state el FQDN que floci-az asignó al container app (algo tipo `pedidos-api.<guid>.azurecontainerapps.io`). Lo extraés sin parsear el state a mano.

**2. El header `Host:`** — este es el mecanismo de enrutamiento de Container Apps. En Azure real, cada app tiene su propio dominio y el tráfico llega al *front door* correcto por DNS. En floci-az todo llega al **mismo puerto (4577)**, así que el enrutamiento hacia tu app se hace con el header `Host`:

```
curl -H "Host: pedidos-api.abc123.azurecontainerapps.io" http://localhost:4577/
                                   └────────┬────────┘
                    floci-az lee este valor y reenvía
                    al contenedor de la réplica de pedidos-api
```

Sin el header, floci-az no sabe a qué app enviar el request y te va a responder un 404 genérico.

---

En resumen: el **paso 2** habilita a Terraform para hablar con el emulador, y el **paso 4** verifica que el microservicio esté realmente desplegado y sirviendo tráfico a través del ingress.

---

## User

Hacelo

---

## Assistant

_Thinking:_

Implement the plan: create docker-compose.yml, .env.example, infra/versions.tf, providers.tf, variables.tf, main.tf, outputs.tf, scripts/trust-cert.ps1, README section.

Let me create files. README: currently "# La-Tiendita". I'll append a section.

Skills check: work-unit-commits? That's for implementation/commits. The skill says "Plan commits as reviewable work units. Trigger: implementation, commit splitting..." Hmm — it's about commits. The user didn't ask for commits. Skill-improver etc don't apply. I could load work-unit-commits since task = implementation. The rule: read matching SKILL.md before generating reply. It arguably matches "implementation". Let me load it to be safe — it may dictate how to group commits. But we're not committing unless asked. Still, mandatory self-check says read if it matches. I'll load it.

Let me create the files first in parallel with reading the skill? Better read skill first (blocking requirement).

**Tool: skill**

**Input:**
```json
{
  "id": "work-unit-commits"
}
```

<skill_content name="work-unit-commits">
# Skill: work-unit-commits

## When to Use

Load this skill when deciding what belongs in each commit or PR.

Use it for:

- Splitting a feature into reviewable work.
- Preparing commits before opening a PR.
- Turning a large change into chained or stacked PRs.
- Keeping reviewer cognitive load healthy.
- Committing ODD work units in reviewable slices.

## Critical Rules

| Rule | Requirement |
|------|-------------|
| Commit by work unit | A commit represents a deliverable behavior, fix, migration, or docs unit. |
| Do not commit by file type | Avoid `models`, then `services`, then `tests` if none works alone. |
| Keep tests with code | Tests belong in the same commit as the behavior they verify. |
| Keep docs with the user-visible change | Docs belong with the feature or workflow they explain. |
| Tell a story | A reviewer should understand why each commit exists from its diff and message. |
| Future PR-ready | Each commit should be a candidate chained PR when the change grows. |
| Budget is not code-golf | Never shrink a diff by deleting comments, blank lines, docs, or tests, or by compressing code, to fit the review budget (400 by default, or the session `review_budget_lines`). Slice by work unit or report the overage. |

## Work Unit Checklist

Before committing, confirm:

- [ ] The commit has one clear purpose.
- [ ] The repo still makes sense after applying only this commit.
- [ ] Tests or docs for this unit are included when relevant.
- [ ] Rollback is reasonable without reverting unrelated work.
- [ ] Focused test command and exact result are recorded.
- [ ] Runtime harness command/scenario and exact result are recorded, or explicit `N/A` explains why no runtime boundary exists.
- [ ] Rollback boundary names the exact files/behavior removable without unrelated work.
- [ ] The commit message explains the outcome, not the file list.

## Split Examples

| Weak split | Better work-unit split |
|------------|------------------------|
| `add models` | `feat(auth): add token validation domain model and tests` |
| `add services` | `feat(auth): wire token validation into login flow` |
| `add tests` | Tests included with each behavior commit |
| `update docs` | Docs included with the user-facing change they explain |

## PR Relationship

Use work-unit commits as the foundation for chained PRs:

1. Build the smallest independent work unit.
2. Include verification for that unit.
3. Commit it with a Conventional Commit message.
4. If the PR approaches 400 changed lines, promote commits or groups of commits into chained PRs.

## ODD Relationship

Organic Driven Development (ODD) commits substantial work in reviewable slices on the feature branch, not one commit per task:

- Commit work units in reviewable slices of about 400 authored changed lines, branch first when on the default branch, with tests and docs alongside the behavior and a Conventional Commit message. A unit whose Risk line names a high-risk item gets its own commit so native review can take it alone, and the last slice closes the feature.
- The native review candidate is that commit, or the PR slice it belongs to when review is deferred, against the previous reviewed boundary. It is never a TODO checkbox and never the accumulated feature branch.
- The running authored line count from work-unit commits feeds the delivery-strategy vocabulary: `ask-on-risk`, `auto-chain`, `single-pr`, `exception-ok`.
- The ODD feature document records the commit that holds each task as evidence and, once a delivery strategy applies, the chosen chain strategy and slice boundaries (which commits each PR holds).

## Commands

```bash
# Review the story before committing
git diff --stat
git diff --cached --stat

# Check recent commit style
git log --oneline -5
```

Base directory for this skill: C:\Users\damian\.config\opencode\skills\work-unit-commits
Relative paths in this skill (e.g., scripts/, reference/) are relative to this base directory.
Note: file list is sampled.

<skill_files>
</skill_files>
</skill_content>

---

## Assistant

_Thinking:_

Now implement: create the files. One work unit — this is a single deliverable (local Azure emulator + Terraform infra). It's a small set of files (~250 lines), fits one commit as one work unit.

Create files.

Creo los archivos del plan.

**Tool: write**

**Input:**
```json
{
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\docker-compose.yml",
  "content": "services:\n  floci-az:\n    image: floci/floci-az:latest\n    ports:\n      - \"4577:4577\"\n    volumes:\n      - /var/run/docker.sock:/var/run/docker.sock # required: Container Apps real mode\n      - ./data:/app/data # persists state + generated TLS cert\n    environment:\n      FLOCI_AZ_TLS_ENABLED: \"true\" # required: azurerm provider discovers the cloud over HTTPS\n      FLOCI_AZ_SERVICES_CONTAINER_APPS_MOCKED: \"false\" # real mode: replicas run as Docker containers\n      FLOCI_AZ_STORAGE_MODE: hybrid # in-memory + async flush every 5s\n    healthcheck:\n      # The image ships no curl/wget; healthcheck.sh is the official probe.\n      test: [\"CMD\", \"healthcheck.sh\"]\n      interval: 5s\n      timeout: 3s\n      retries: 10\n"
}
```

Created file successfully: docker-compose.yml

**Tool: write**

**Input:**
```json
{
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\.env.example",
  "content": "# ── floci-az ──────────────────────────────────────────────\nFLOCI_AZ_URL=http://localhost:4577\n\n# Standard devstoreaccount1 connection string (credentials are not validated in dev mode).\n# Point endpoints at `floci-az` (compose network) when your code runs inside a container.\nAZURE_STORAGE_CONNECTION_STRING=\"DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:4577/devstoreaccount1;QueueEndpoint=http://localhost:4577/devstoreaccount1-queue;TableEndpoint=http://localhost:4577/devstoreaccount1-table;\"\n\n# ── Terraform ─────────────────────────────────────────────\nTF_VAR_location=\"eastus\"\nTF_VAR_image_pedidos=\"latiendita/pedidos-api:local\"\n"
}
```

Permission denied: edit

**Tool: write**

**Input:**
```json
{
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\infra\\versions.tf",
  "content": "terraform {\n  required_version = \">= 1.5.0\"\n\n  required_providers {\n    azurerm = {\n      source  = \"hashicorp/azurerm\"\n      version = \">= 3.0\" # works with the 3.x and 4.x provider lines\n    }\n  }\n}\n"
}
```

Created file successfully: infra/versions.tf

**Tool: write**

**Input:**
```json
{
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\infra\\providers.tf",
  "content": "provider \"azurerm\" {\n  features {}\n\n  use_cli = false\n\n  # Custom cloud pointing at floci-az. The provider discovers the cloud over\n  # HTTPS at /metadata/endpoints, which is why TLS must be enabled on the\n  # emulator (FLOCI_AZ_TLS_ENABLED=true) and its certificate trusted locally.\n  environment   = \"stack\"\n  metadata_host = \"localhost:4577\"\n\n  # Placeholder credentials: floci-az accepts any value without validating it.\n  subscription_id = \"00000000-0000-0000-0000-000000000001\"\n  tenant_id       = \"00000000-0000-0000-0000-000000000002\"\n  client_id       = \"00000000-0000-0000-0000-000000000003\"\n  client_secret   = \"fake-secret\"\n}\n"
}
```

Created file successfully: infra/providers.tf

**Tool: write**

**Input:**
```json
{
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\infra\\variables.tf",
  "content": "variable \"location\" {\n  description = \"Azure location. floci-az accepts any value.\"\n  type        = string\n  default     = \"eastus\"\n}\n\nvariable \"image_pedidos\" {\n  description = \"Local Docker image for the orders microservice. It must already exist in the local Docker daemon because floci-az starts revision replicas from it.\"\n  type        = string\n  default     = \"latiendita/pedidos-api:local\"\n}\n"
}
```

Created file successfully: infra/variables.tf

---

## Assistant

_Thinking:_

.env.example denied — likely hidden/dotfile protection or .env pattern. Let me try again... Maybe permission denied for dotfiles matching .env*. I could try writing as `env.example`? Better ask? First retry once to see. Or maybe the .env prefix is blocked by permission rules (secret protection). Let's attempt again; if fails, tell user and offer to create `.env.example` content in README or ask them to allow.

**Tool: write**

**Input:**
```json
{
  "content": "# ── floci-az ──────────────────────────────────────────────\nFLOCI_AZ_URL=http://localhost:4577\n\n# Standard devstoreaccount1 connection string (credentials are not validated in dev mode).\n# Point endpoints at `floci-az` (compose network) when your code runs inside a container.\nAZURE_STORAGE_CONNECTION_STRING=\"DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:4577/devstoreaccount1;QueueEndpoint=http://localhost:4577/devstoreaccount1-queue;TableEndpoint=http://localhost:4577/devstoreaccount1-table;\"\n\n# ── Terraform ─────────────────────────────────────────────\nTF_VAR_location=\"eastus\"\nTF_VAR_image_pedidos=\"latiendita/pedidos-api:local\"\n",
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\.env.example"
}
```

Permission denied: edit

---

## Assistant

_Thinking:_

.env.example blocked (likely a permission rule against .env* writes — sensible: env files could contain secrets). I'll continue with the rest and tell the user.

Next: infra/main.tf, infra/outputs.tf, scripts/trust-cert.ps1, README section.

`.env.example` está bloqueado por permisos (regla de protección sobre archivos `.env*`). Sigo con el resto y te lo aviso.

**Tool: write**

**Input:**
```json
{
  "content": "resource \"azurerm_resource_group\" \"this\" {\n  name     = \"latiendita\"\n  location = var.location\n}\n\n# ── Container Apps managed environment ────────────────────\nresource \"azurerm_container_app_environment\" \"this\" {\n  name                = \"latiendita-env\"\n  location            = azurerm_resource_group.this.location\n  resource_group_name = azurerm_resource_group.this.name\n}\n\n# ── Orders microservice ───────────────────────────────────\nresource \"azurerm_container_app\" \"pedidos\" {\n  name                         = \"pedidos-api\"\n  resource_group_name          = azurerm_resource_group.this.name\n  location                     = azurerm_resource_group.this.location\n  container_app_environment_id = azurerm_container_app_environment.this.id\n  revision_mode                = \"Single\"\n\n  template {\n    container {\n      name   = \"pedidos\"\n      image  = var.image_pedidos\n      cpu    = 0.5\n      memory = \"1Gi\"\n\n      ports {\n        name           = \"http\"\n        container_port = 8080\n      }\n\n      env {\n        name  = \"AZURE_STORAGE_CONNECTION_STRING\"\n        value = \"DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://floci-az:4577/devstoreaccount1;QueueEndpoint=http://floci-az:4577/devstoreaccount1-queue;TableEndpoint=http://floci-az:4577/devstoreaccount1-table;\"\n      }\n    }\n\n    min_replicas = 1\n    max_replicas = 2\n  }\n\n  ingress {\n    external_enabled = true\n    target_port      = 8080\n    transport        = \"http\"\n\n    traffic_weight {\n      latest_revision = true\n      percentage      = 100\n    }\n  }\n}\n",
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\infra\\main.tf"
}
```

Created file successfully: infra/main.tf

**Tool: write**

**Input:**
```json
{
  "content": "output \"resource_group\" {\n  value = azurerm_resource_group.this.name\n}\n\noutput \"pedidos_fqdn\" {\n  description = \"FQDN assigned by floci-az to the container app.\"\n  value       = azurerm_container_app.pedidos.ingress[0].fqdn\n}\n\noutput \"invoke\" {\n  description = \"How to call the service through the emulator.\"\n  value       = \"curl -H \\\"Host: ${azurerm_container_app.pedidos.ingress[0].fqdn}\\\" http://localhost:4577/\"\n}\n",
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\infra\\outputs.tf"
}
```

Created file successfully: infra/outputs.tf

**Tool: write**

**Input:**
```json
{
  "content": "# Downloads the floci-az self-signed certificate and installs it in the current\n# user's trust store (no admin rights required). Prerequisite: FLOCI_AZ_TLS_ENABLED=true.\n$ErrorActionPreference = \"Stop\"\n$ProgressPreference    = \"SilentlyContinue\"\n\n$out = Join-Path $PSScriptRoot \"floci-az.crt\"\nInvoke-WebRequest -Uri \"http://localhost:4577/_floci/tls-cert\" -OutFile $out\nImport-Certificate -FilePath $out -CertStoreLocation Cert:\\CurrentUser\\Root | Out-Null\n\nWrite-Host \"floci-az certificate installed in Cert:\\CurrentUser\\Root\"\n",
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\scripts\\trust-cert.ps1"
}
```

Created file successfully: scripts/trust-cert.ps1

---

## Assistant

_Thinking:_

Now README: append the section. Current README: "# La-Tiendita". Include .env.example content inside README since the file couldn't be created? I'll add a note that .env.example needs creating manually — better: include the exact content in README so user can paste. Actually let me first check whether the .env.example block is a platform rule I shouldn't circumvent... The permission rejected means user's permission config denied writing .env* files. Providing content in README for the user to paste is fine (it's documentation). I'll include a short "Environment variables" section with the connection string.

Let me edit README.

**Tool: write**

**Input:**
```json
{
  "content": "# La-Tiendita\n\n## Azure local con floci-az + Terraform\n\nLos microservicios se despliegan como Azure Container Apps contra el emulador\nlocal [floci-az](https://github.com/floci-io/floci-az) (puerto 4577).\n\n### 1. Levantar el emulador\n\n```powershell\ndocker compose up -d\ndocker compose ps   # wait until STATUS: healthy\n```\n\n### 2. Confiar el certificado TLS (una vez por máquina)\n\n```powershell\n.\\scripts\\trust-cert.ps1\n```\n\nfloci-az genera un certificado auto-firmado porque el provider `azurerm`\ndescubre la nube por HTTPS. Sin este paso Terraform falla con\n`x509: certificate signed by unknown authority`.\n\n### 3. Variables de entorno\n\nCopiá `.env.example` a `.env` y completá los valores:\n\n```bash\n# ── floci-az ──────────────────────────────────────────────\nFLOCI_AZ_URL=http://localhost:4577\n\n# Standard devstoreaccount1 connection string (credentials are not validated in dev mode).\n# Point endpoints at `floci-az` (compose network) when your code runs inside a container.\nAZURE_STORAGE_CONNECTION_STRING=\"DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:4577/devstoreaccount1;QueueEndpoint=http://localhost:4577/devstoreaccount1-queue;TableEndpoint=http://localhost:4577/devstoreaccount1-table;\"\n\n# ── Terraform ─────────────────────────────────────────────\nTF_VAR_location=\"eastus\"\nTF_VAR_image_pedidos=\"latiendita/pedidos-api:local\"\n```\n\n### 4. Desplegar los microservicios\n\n```powershell\n# The image must exist in the local daemon: floci-az starts replicas from it\ndocker build -t latiendita/pedidos-api:local ./services/pedidos\n\ncd infra\nterraform init\nterraform apply\n```\n\n### 5. Invocar\n\n```powershell\n$FQDN = terraform -chdir=infra output -raw pedidos_fqdn\ncurl -H \"Host: $FQDN\" http://localhost:4577/\n```\n\nThe `Host` header is how floci-az routes the request to your app: every app\nshares port 4577, so the FQDN identifies the target.\n\n### 6. Consola web\n\nAbrir `http://localhost:4577/_floci/ui`\n\n### Estructura\n\n```\nLaTiendita/\n├── docker-compose.yml        # floci-az: TLS, Container Apps real mode, hybrid storage\n├── .env.example              # connection string + Terraform vars (see above)\n├── infra/                    # Terraform (azurerm → floci-az)\n│   ├── versions.tf\n│   ├── providers.tf          # environment = \"stack\", metadata_host = localhost:4577\n│   ├── variables.tf\n│   ├── main.tf               # resource group + managed env + pedidos-api\n│   └── outputs.tf\n├── scripts/trust-cert.ps1    # install floci-az cert in CurrentUser\\Root\n└── services/pedidos/         # microservice + its Dockerfile\n```\n",
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\README.md"
}
```

Wrote file successfully: README.md

---

## Assistant

_Thinking:_

Now verify: terraform fmt/validate would be good but terraform may not be installed. Also docker compose config validation. Let's run checks: `terraform -version`, `docker compose config -q`.

Also validate that azurerm provider schema matches — running `terraform init` needs network; maybe just `terraform fmt -check` and `terraform validate` requires init. Let's try quick checks.

**Tool: shell**

**Input:**
```json
{
  "command": "terraform -version; docker compose version",
  "workdir": "C:\\Users\\damian\\Repositorios\\LaTiendita"
}
```

terraform : El t�rmino 'terraform' no se reconoce como nombre de un cmdlet, funci�n, archivo de script o programa 
ejecutable. Compruebe si escribi� correctamente el nombre o, si incluy� una ruta de acceso, compruebe que dicha ruta 
es correcta e int�ntelo de nuevo.
En l�nea: 1 Car�cter: 1
+ terraform -version; docker compose version
+ ~~~~~~~~~
    + CategoryInfo          : ObjectNotFound: (terraform:String) [], CommandNotFoundException
    + FullyQualifiedErrorId : CommandNotFoundException
 
Docker Compose version v5.5.1


**Tool: shell**

**Input:**
```json
{
  "command": "docker compose config --quiet && echo COMPOSE_OK",
  "workdir": "C:\\Users\\damian\\Repositorios\\LaTiendita"
}
```

En l�nea: 1 Car�cter: 31
+ docker compose config --quiet && echo COMPOSE_OK
+                               ~~
El token '&&' no es un separador de instrucciones v�lido en esta versi�n.
    + CategoryInfo          : ParserError: (:) [], ParentContainsErrorRecordException
    + FullyQualifiedErrorId : InvalidEndOfLine
 

Exited with code 1

---

## Assistant

_Thinking:_

PowerShell 5.1 — no `&&`. Run compose config alone. Also terraform not installed — note it. Let's validate compose and maybe tofu (opentofu)? Check `tofu -version`.

**Tool: shell**

**Input:**
```json
{
  "command": "docker compose config --quiet; if ($LASTEXITCODE -eq 0) { echo COMPOSE_OK }; tofu -version",
  "workdir": "C:\\Users\\damian\\Repositorios\\LaTiendita"
}
```

COMPOSE_OK
tofu : El t�rmino 'tofu' no se reconoce como nombre de un cmdlet, funci�n, archivo de script o programa ejecutable. 
Compruebe si escribi� correctamente el nombre o, si incluy� una ruta de acceso, compruebe que dicha ruta es correcta e 
int�ntelo de nuevo.
En l�nea: 1 Car�cter: 78
+ ... g --quiet; if ($LASTEXITCODE -eq 0) { echo COMPOSE_OK }; tofu -versio ...
+                                                              ~~~~
    + CategoryInfo          : ObjectNotFound: (tofu:String) [], CommandNotFoundException
    + FullyQualifiedErrorId : CommandNotFoundException
 

Exited with code 1

---

## Assistant

_Thinking:_

Compose OK. Neither terraform nor tofu installed. I can't validate HCL locally without terraform. I could at least verify the azurerm schema details with context7 docs? The critical claims: azurerm_container_app fields. I'm fairly confident: `ingress { external_enabled, target_port, transport, traffic_weight { percentage, latest_revision } }`, `template { container { ports { name, container_port } env { name, value } } min_replicas max_replicas }`. Also `azurerm_container_app_environment` requires name, location, resource_group_name — yes.

One thing: in azurerm 4.x, provider requires `resource_provider_registrations`? Docs said not needed. Fine.

Note about PowerShell 5.1: `Invoke-WebRequest` on PS5.1 may need TLS for https; our URI is http, fine. But PS5.1 IE engine may fail without `-UseBasicParsing`? In Windows PowerShell 5.1, Invoke-WebRequest requires IE first-launch unless -UseBasicParsing. Actually on servers without IE, it errors. Safe to add `-UseBasicParsing`. Let me add that flag for compatibility.

Also the cert download returns a PEM? `GET /_floci/tls-cert` — README says "Fetch the active certificate PEM from GET /_floci/tls-cert". Also troubleshooting said it returns JSON `{"tlsEnabled": false}` when disabled — so when enabled it may return... hmm. Terraform docs step 2: `curl -sf http://localhost:4577/_floci/tls-cert -o floci-az.crt` — treats it as raw cert file. When TLS disabled it returns JSON `{"tlsEnabled": false}`. So with TLS enabled, it returns the PEM (or JSON containing it?). Official docs use `-o floci-az.crt`, so raw PEM. But to be robust, the script could check content. Keep simple but add a check: if content starts with "{" then error TLS not enabled. That's a nice safety.

Import-Certificate exists in Windows PowerShell 5.1 (PKI module) — yes, available in Windows 10+.

Let me update the script: add -UseBasicParsing and validate the response looks like a certificate.

Should I test docker compose up? That would pull image and start the emulator — reasonable verification, but heavyweight (image pull ~ tens of MB). It'd be a real verification. The user said "Hacelo" — implement. Starting the emulator locally validates config. I think it's worth doing: `docker compose up -d`, wait, check health, then optionally download cert. That verifies steps 1-2 of README. Risk: pulls image (~13MB native? plus more). Let's try with a reasonable timeout.

Let me first update script, then run compose up in background? Foreground with timeout 180s.

**Tool: edit**

**Input:**
```json
{
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\scripts\\trust-cert.ps1",
  "oldString": "$out = Join-Path $PSScriptRoot \"floci-az.crt\"\nInvoke-WebRequest -Uri \"http://localhost:4577/_floci/tls-cert\" -OutFile $out\nImport-Certificate -FilePath $out -CertStoreLocation Cert:\\CurrentUser\\Root | Out-Null",
  "newString": "$out = Join-Path $PSScriptRoot \"floci-az.crt\"\nInvoke-WebRequest -Uri \"http://localhost:4577/_floci/tls-cert\" -OutFile $out -UseBasicParsing\n\n# With TLS disabled the endpoint answers JSON instead of a PEM certificate.\n$body = Get-Content $out -Raw\nif ($body -notlike \"*BEGIN CERTIFICATE*\") {\n    throw \"floci-az did not return a certificate (TLS disabled?). Check FLOCI_AZ_TLS_ENABLED=true.\"\n}\n\nImport-Certificate -FilePath $out -CertStoreLocation Cert:\\CurrentUser\\Root | Out-Null"
}
```

Edited scripts/trust-cert.ps1 (1 replacement)

**Tool: shell**

**Input:**
```json
{
  "command": "docker compose up -d",
  "timeout": 300000,
  "workdir": "C:\\Users\\damian\\Repositorios\\LaTiendita"
}
```

failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine; check if the path is correct and if the daemon is running: open //./pipe/dockerDesktopLinuxEngine: El sistema no puede encontrar el archivo especificado.

Exited with code 1

---

## Assistant

_Thinking:_

Docker Desktop isn't running. Options: start Docker Desktop (heavy) or report. I can try starting "Docker Desktop" app. That's a big action; but it enables verification. Let me try launching it in background and wait. The user is likely to want that anyway to run floci-az. Let's attempt: start "Docker Desktop" and poll for the daemon.

I'll run a background command to start Docker Desktop, then check.

Docker Desktop no está corriendo. Lo levanto para poder verificar el stack de verdad:

**Tool: shell**

**Input:**
```json
{
  "command": "Start-Process \"C:\\Program Files\\Docker\\Docker\\Docker Desktop.exe\"; echo LANZADO"
}
```

Start-Process : Este comando no se puede ejecutar debido al error: El sistema no puede encontrar el archivo 
especificado.
En l�nea: 1 Car�cter: 1
+ Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"; ec ...
+ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    + CategoryInfo          : InvalidOperation: (:) [Start-Process], InvalidOperationException
    + FullyQualifiedErrorId : InvalidOperationException,Microsoft.PowerShell.Commands.StartProcessCommand
 
LANZADO


---

## Assistant

_Thinking:_

Docker Desktop not at that path. But compose v5.5.1 plugin exists... maybe docker engine elsewhere (podman? Rancher Desktop? WSL?). Check where docker is: `Get-Command docker`, and look for other engines: Rancher Desktop path %LOCALAPPDATA%\Programs\Rancher Desktop, or check `wsl -l -v`.

Let's investigate quickly.

**Tool: shell**

**Input:**
```json
{
  "command": "Get-Command docker | Select-Object -ExpandProperty Source; wsl -l -v 2>$null; Test-Path \"$env:LOCALAPPDATA\\Programs\\Rancher Desktop\"; Test-Path \"$env:ProgramFiles\\Rancher Desktop\\Rancher Desktop.exe\"; Test-Path \"$env:ProgramFiles\\Podman\\podman.exe\""
}
```

C:\Users\damian\AppData\Local\Programs\DockerDesktop\resources\bin\docker.exe
    N A M E                             S T A T E                       V E R S I O N  
 *   d o c k e r - d e s k t o p         S t o p p e d                   2  
 False
False
False


---

## Assistant

_Thinking:_

Docker Desktop installed at %LOCALAPPDATA%\Programs\DockerDesktop. Start it there.

**Tool: shell**

**Input:**
```json
{
  "command": "Start-Process \"$env:LOCALAPPDATA\\Programs\\DockerDesktop\\bin\\Docker Desktop.exe\" -ErrorAction SilentlyContinue; if (-not $?) { Start-Process \"$env:LOCALAPPDATA\\Programs\\DockerDesktop\\Docker Desktop.exe\" -ErrorAction SilentlyContinue }; Get-ChildItem \"$env:LOCALAPPDATA\\Programs\\DockerDesktop\" -Filter \"*.exe\" | Select-Object -ExpandProperty FullName"
}
```

Start-Process : Este comando no se puede ejecutar debido al error: El sistema no puede encontrar el archivo 
especificado.
En l�nea: 1 Car�cter: 1
+ Start-Process "$env:LOCALAPPDATA\Programs\DockerDesktop\bin\Docker De ...
+ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    + CategoryInfo          : InvalidOperation: (:) [Start-Process], InvalidOperationException
    + FullyQualifiedErrorId : InvalidOperationException,Microsoft.PowerShell.Commands.StartProcessCommand
 
C:\Users\damian\AppData\Local\Programs\DockerDesktop\7zr.exe
C:\Users\damian\AppData\Local\Programs\DockerDesktop\courgette64.exe
C:\Users\damian\AppData\Local\Programs\DockerDesktop\Docker Desktop Installer.exe
C:\Users\damian\AppData\Local\Programs\DockerDesktop\Docker Desktop.exe
C:\Users\damian\AppData\Local\Programs\DockerDesktop\DockerCli.exe
C:\Users\damian\AppData\Local\Programs\DockerDesktop\InstallerCli.exe


---

## Assistant

_Thinking:_

The exe exists: C:\Users\damian\AppData\Local\Programs\DockerDesktop\Docker Desktop.exe. My first attempt with SilentlyContinue still failed (odd — second Start-Process showed error but with -ErrorAction SilentlyContinue? It printed... whatever). Actually error printed was for the first Start-Process (ErrorAction SilentlyContinue should suppress...). Hmm, the error shows the bin path. Whatever — start the right path explicitly.

**Tool: shell**

**Input:**
```json
{
  "command": "Start-Process \"$env:LOCALAPPDATA\\Programs\\DockerDesktop\\Docker Desktop.exe\"; echo INICIADO"
}
```

INICIADO


---

## Assistant

_Thinking:_

Now wait for daemon. Poll docker info with a loop, up to ~3 min.

**Tool: shell**

**Input:**
```json
{
  "command": "$deadline=(Get-Date).AddMinutes(3); do { Start-Sleep -Seconds 5; docker info --format \"{{.ServerVersion}}\" 2>$null; $ok = ($LASTEXITCODE -eq 0) } while (-not $ok -and (Get-Date) -lt $deadline); if ($ok) { echo DAEMON_OK } else { echo TIMEOUT }",
  "timeout": 200000
}
```

29.8.1
DAEMON_OK


---

## Assistant

_Thinking:_

Docker daemon is up. Now `docker compose up -d` in the repo (pulls floci-az image), wait for healthy, then run trust-cert script? Import-Certificate to CurrentUser\Root would modify user's trust store — that's a meaningful machine change. The README says run it. Should I run it? It installs a self-signed root CA for the user. It's part of the plan the user approved ("Hacelo"). But installing a root cert is a security-relevant action. I'd rather verify up to health, then ask/let the user run the cert script themselves. Actually the plan approved includes it; but self-signed root trust is the kind of thing to confirm. I'll run compose up, verify health, then report and let the user decide about installing the cert (it's a machine-level trust change).

Let's do compose up -d (background pull may take a while).

Daemon arriba. Levanto floci-az:

**Tool: shell**

**Input:**
```json
{
  "command": "docker compose up -d; docker compose ps",
  "timeout": 420000,
  "workdir": "C:\\Users\\damian\\Repositorios\\LaTiendita"
}
```

 Image floci/floci-az:latest Pulling 
 923c431fc5af Pulling fs layer 0B
 3c68fad3d37a Pulling fs layer 0B
 fd49915532c8 Pulling fs layer 0B
 6813daa0a7a9 Pulling fs layer 0B
 7bff82a6f8ab Pulling fs layer 0B
 c6a813a8b6cb Pulling fs layer 0B
 c6a813a8b6cb Download complete 0B
 44136fa355b3 Already exists 0B
 923c431fc5af Download complete 0B
 6813daa0a7a9 Download complete 0B
 3c68fad3d37a Download complete 0B
 40c3a218d44f Download complete 0B
 fd49915532c8 Downloading 1.049MB
 7bff82a6f8ab Downloading 1.049MB
 fd49915532c8 Downloading 1.049MB
 7bff82a6f8ab Downloading 1.049MB
 fd49915532c8 Downloading 2.097MB
 7bff82a6f8ab Downloading 2.097MB
 fd49915532c8 Downloading 3.146MB
 7bff82a6f8ab Downloading 3.146MB
 fd49915532c8 Downloading 4.194MB
 7bff82a6f8ab Downloading 3.146MB
 fd49915532c8 Downloading 5.243MB
 7bff82a6f8ab Downloading 4.194MB
 7bff82a6f8ab Downloading 5.243MB
 fd49915532c8 Downloading 5.243MB
 7bff82a6f8ab Downloading 6.291MB
 fd49915532c8 Downloading 6.291MB
 7bff82a6f8ab Downloading 7.34MB
 fd49915532c8 Downloading 7.269MB
 7bff82a6f8ab Downloading 8.389MB
 fd49915532c8 Download complete 0B
 7bff82a6f8ab Downloading 8.389MB
 fd49915532c8 Extracting 1B
 7bff82a6f8ab Downloading 9.437MB
 fd49915532c8 Extracting 1B
 7bff82a6f8ab Downloading 9.437MB
 fd49915532c8 Extracting 1B
 7bff82a6f8ab Downloading 10.49MB
 fd49915532c8 Extracting 1B
 7bff82a6f8ab Downloading 11.53MB
 fd49915532c8 Extracting 1B
 7bff82a6f8ab Downloading 12.58MB
 fd49915532c8 Extracting 1B
 7bff82a6f8ab Downloading 13.63MB
 fd49915532c8 Extracting 1B
 7bff82a6f8ab Downloading 14.68MB
 fd49915532c8 Extracting 1B
 7bff82a6f8ab Downloading 15.73MB
 6813daa0a7a9 Pull complete 0B
 3c68fad3d37a Extracting 1B
 fd49915532c8 Pull complete 0B
 7bff82a6f8ab Downloading 16.78MB
 3c68fad3d37a Pull complete 0B
 7bff82a6f8ab Downloading 17.83MB
 7bff82a6f8ab Downloading 18.87MB
 7bff82a6f8ab Downloading 19.92MB
 7bff82a6f8ab Downloading 20.97MB
 7bff82a6f8ab Downloading 22.02MB
 7bff82a6f8ab Downloading 23.07MB
 7bff82a6f8ab Downloading 24.12MB
 7bff82a6f8ab Downloading 25.17MB
 7bff82a6f8ab Downloading 26.21MB
 7bff82a6f8ab Downloading 27.26MB
 7bff82a6f8ab Downloading 28.31MB
 7bff82a6f8ab Downloading 29.36MB
 7bff82a6f8ab Downloading 30.41MB
 7bff82a6f8ab Downloading 31.46MB
 7bff82a6f8ab Downloading 32.51MB
 7bff82a6f8ab Downloading 33.55MB
 7bff82a6f8ab Downloading 34.6MB
 7bff82a6f8ab Downloading 35.65MB
 7bff82a6f8ab Downloading 36.7MB
 7bff82a6f8ab Downloading 37.75MB
 7bff82a6f8ab Downloading 38.8MB
 7bff82a6f8ab Downloading 39.85MB
 7bff82a6f8ab Downloading 39.85MB
 7bff82a6f8ab Downloading 41.43MB
 7bff82a6f8ab Downloading 41.43MB
 7bff82a6f8ab Downloading 41.43MB
 7bff82a6f8ab Downloading 41.43MB
 7bff82a6f8ab Downloading 41.43MB
 7bff82a6f8ab Downloading 41.43MB
 7bff82a6f8ab Downloading 41.43MB
 7bff82a6f8ab Download complete 0B
 7bff82a6f8ab Extracting 1B
 7bff82a6f8ab Extracting 1B
 7bff82a6f8ab Extracting 1B
 7bff82a6f8ab Extracting 1B
 7bff82a6f8ab Extracting 1B
 7bff82a6f8ab Extracting 1B
 7bff82a6f8ab Extracting 1B
 7bff82a6f8ab Extracting 1B
 7bff82a6f8ab Pull complete 0B
 c6a813a8b6cb Pull complete 0B
 923c431fc5af Pull complete 0B
 Image floci/floci-az:latest Pulled 
 Network latiendita_default Creating 
 Network latiendita_default Creating 
 Network latiendita_default Created 
 Network latiendita_default Created 
 Container latiendita-floci-az-1 Creating 
 Container latiendita-floci-az-1 Created 
 Container latiendita-floci-az-1 Starting 
 Container latiendita-floci-az-1 Started 
NAME                    IMAGE                   COMMAND                  SERVICE    CREATED         STATUS                                     PORTS
latiendita-floci-az-1   floci/floci-az:latest   "/usr/local/bin/dock…"   floci-az   3 seconds ago   Up Less than a second (health: starting)   0.0.0.0:4577->4577/tcp, [::]:4577->4577/tcp


---

## Assistant

_Thinking:_

Now wait for healthy and test endpoints: /_floci/health, TLS cert endpoint.

**Tool: shell**

**Input:**
```json
{
  "command": "$deadline=(Get-Date).AddMinutes(2); do { Start-Sleep -Seconds 4; $s = docker inspect --format '{{.State.Health.Status}}' latiendita-floci-az-1 } while ($s -ne \"healthy\" -and (Get-Date) -lt $deadline); echo \"HEALTH: $s\"; (Invoke-WebRequest -Uri \"http://localhost:4577/_floci/health\" -UseBasicParsing).Content; $c=(Invoke-WebRequest -Uri \"http://localhost:4577/_floci/tls-cert\" -UseBasicParsing).Content; if ($c -like \"*BEGIN CERTIFICATE*\") { echo \"TLS CERT: PEM OK\" } else { echo \"TLS CERT: $c\" }",
  "timeout": 180000
}
```

HEALTH: healthy
{"status":"UP","version":"0.16.0","edition":"floci-az-always-free"}
TLS CERT: 45 45 45 45 45 66 69 71 73 78 32 67 69 82 84 73 70 73 67 65 84 69 45 45 45 45 45 10 77 73 73 67 43 84 67 67 65 101 71 103 65 119 73 66 65 103 73 82 65 76 55 72 111 106 122 121 103 68 117 103 88 120 101 53 105 88 105 78 117 107 115 119 68 81 89 74 75 111 90 73 104 118 99 78 65 81 69 78 66 81 65 119 10 70 106 69 85 77 66 73 71 65 49 85 69 65 119 119 76 90 109 120 118 89 50 107 116 89 88 111 116 89 50 69 119 72 104 99 78 77 106 89 120 77 84 65 122 77 84 85 121 79 68 69 120 87 104 99 78 77 122 89 120 77 68 77 120 10 77 84 85 121 79 68 69 120 87 106 65 87 77 82 81 119 69 103 89 68 86 81 81 68 68 65 116 109 98 71 57 106 97 83 49 104 101 105 49 106 89 84 67 67 65 83 73 119 68 81 89 74 75 111 90 73 104 118 99 78 65 81 69 66 10 66 81 65 68 103 103 69 80 65 68 67 67 65 81 111 67 103 103 69 66 65 78 86 54 50 67 120 51 76 84 100 71 82 49 120 115 105 65 70 56 47 74 77 50 71 67 102 77 66 67 106 118 99 113 98 78 90 77 105 68 80 76 118 74 10 86 72 52 120 50 48 108 71 87 47 56 112 73 47 101 82 79 118 50 43 105 47 87 47 81 67 54 120 110 114 72 66 77 112 66 66 82 79 103 119 116 100 108 119 108 78 70 120 66 113 118 70 117 47 118 82 72 84 104 99 116 73 116 98 10 54 76 86 110 89 108 51 86 68 82 65 57 57 55 50 106 109 79 114 106 102 116 70 107 109 104 50 118 48 97 73 54 53 74 57 115 116 70 85 78 84 108 111 111 97 88 79 79 119 68 107 83 72 76 121 104 51 81 50 73 102 84 117 99 10 79 115 87 49 56 65 66 43 88 90 80 79 101 103 53 47 86 71 77 50 79 104 104 97 43 69 117 69 111 105 50 97 117 67 89 120 75 107 78 52 88 118 82 102 89 73 103 106 56 75 57 77 76 56 115 79 115 105 106 106 106 117 80 68 10 53 86 75 109 118 80 98 53 67 107 104 69 70 84 113 69 77 100 119 115 84 105 72 55 68 100 68 83 70 78 50 109 49 116 68 105 43 103 77 50 76 78 111 103 55 74 53 118 69 110 76 82 83 51 73 110 114 111 122 81 71 84 104 77 10 51 121 54 67 54 113 56 67 67 120 110 118 50 85 55 105 117 110 69 48 98 65 57 80 106 66 81 105 100 105 119 97 109 69 66 70 48 106 113 107 85 71 99 67 65 119 69 65 65 97 78 67 77 69 65 119 68 119 89 68 86 82 48 84 10 65 81 72 47 66 65 85 119 65 119 69 66 47 122 65 79 66 103 78 86 72 81 56 66 65 102 56 69 66 65 77 67 65 81 89 119 72 81 89 68 86 82 48 79 66 66 89 69 70 76 99 80 112 101 50 66 53 107 107 73 75 112 120 47 10 79 67 116 66 116 116 80 108 114 119 84 76 77 65 48 71 67 83 113 71 83 73 98 51 68 81 69 66 68 81 85 65 65 52 73 66 65 81 67 75 43 97 84 117 71 88 90 83 50 51 48 115 121 76 116 47 111 108 51 103 104 78 49 49 10 88 83 48 55 83 87 120 67 65 79 47 68 98 110 100 75 112 83 100 71 71 55 105 86 56 69 55 119 105 85 120 43 100 73 65 100 51 111 88 50 109 73 47 66 120 51 101 114 53 55 57 74 75 110 99 103 100 72 111 65 100 72 85 72 10 51 81 107 68 70 80 119 106 70 109 76 76 69 49 106 71 70 109 54 109 87 105 98 102 47 119 81 53 117 76 78 97 48 113 112 112 52 106 50 101 43 51 49 116 110 101 106 118 69 89 103 89 48 101 107 117 65 47 97 75 69 72 82 122 10 49 87 115 105 117 83 90 52 75 115 118 111 106 52 78 80 72 74 57 103 53 99 83 56 56 83 115 48 104 110 109 80 107 90 57 66 48 52 71 109 99 77 113 113 75 70 115 47 101 54 54 86 77 116 75 70 105 73 79 43 49 103 121 53 10 74 56 79 53 69 86 71 122 72 82 72 55 103 107 106 97 48 69 115 79 70 73 76 56 109 85 76 105 85 81 84 78 56 67 98 117 56 50 114 54 57 85 106 51 121 81 119 81 122 122 120 83 77 89 66 109 114 107 102 47 47 79 100 112 10 70 98 104 107 79 87 110 72 68 71 104 104 73 115 81 57 66 116 56 78 88 114 57 114 87 98 111 104 113 108 117 76 54 55 87 81 66 112 72 55 104 90 68 50 105 122 74 116 122 118 103 77 48 56 51 88 116 108 49 48 10 45 45 45 45 45 69 78 68 32 67 69 82 84 73 70 73 67 65 84 69 45 45 45 45 45 10


---

## Assistant

_Thinking:_

TLS cert returns PEM (the "byte array" display is just PowerShell rendering the string oddly — content starts with "-----BEGIN CERTIFICATE-----" i.e., 45 45 45...). Good.

Emulator healthy v0.16.0, TLS cert available.

Remaining verification: terraform not installed → cannot run init/validate. Should I install Terraform? That's a big machine change; better to ask the user. Also installing the root cert (trust-cert.ps1) modifies trust store — I'll ask.

Also .env.example write blocked — the user may want to adjust permissions.

Also should I test a blob operation? Quick smoke test: create container via REST? Could validate storage with a simple PUT request requires auth signing... skip; auth not validated (dev mode) — Blob REST requires Authorization header but SharedKey not validated... Actually FLOCI_AZ_AUTH_MODE dev "every value accepts any credentials without validating SharedKey signatures" — but a header must probably be present. Could try with dummy header. That's extra; not needed.

Optionally verify storage with `az`? Not installed. Skip.

Now memory save (progress: implemented), then final reply. Also note memory earlier saved.

Save memory: implementation done, files, blockers (.env.example permission, terraform missing, Docker Desktop was off).

Then final message: summary + Key Learnings? The passive capture says include "## Key Learnings:" on task completion. Let's include a short one.

Decide about committing: user didn't ask. Skip commits (no git operations without ask? Conventional commits rule only applies if committing). Leave uncommitted, mention.

Final message should ask about installing cert + terraform? Rule: one question at a time. The next action needs user decision (install root cert, install Terraform). Ask one: whether to run trust-cert + install Terraform... that's two. Ask the most important: install Terraform to validate infra? Hmm. Cert installation is prerequisite for terraform. I'd ask: "¿Ejecuto trust-cert.ps1 (instala cert en tu store) y instalo Terraform para validar?" — it's a combined yes/no. That's effectively one question.

**Tool: execute**

**Input:**
```json
{
  "code": "const r = await tools.engram.mem_save({\n  title: \"Implementado stack floci-az en LaTiendita\",\n  type: \"config\",\n  topic_key: \"infra/floci-az-emulator\",\n  content: \"**What**: Created docker-compose.yml, infra/*.tf, scripts/trust-cert.ps1 and README workflow for local Azure emulation with floci-az + Terraform.\\n**Why**: User approved the plan to deploy microservices as Container Apps on the emulator.\\n**Where**: docker-compose.yml, README.md, infra/versions.tf, infra/providers.tf, infra/variables.tf, infra/main.tf, infra/outputs.tf, scripts/trust-cert.ps1\\n**Learned**: (1) Writing .env.example is blocked by workspace permissions (permission.rejected on .env*) — env var content moved into README; (2) floci-az 0.16.0 verified running: /_floci/health returns UP, /_floci/tls-cert returns PEM when TLS enabled; (3) docker compose config --quiet passes; (4) terraform/tofu NOT installed on machine — infra/ still unvalidated; (5) Docker Desktop lives at %LOCALAPPDATA%\\\\Programs\\\\DockerDesktop (not Program Files) and was stopped.\"\n});\nreturn r;"
}
```

{
  "id": 13,
  "judgment_required": false,
  "project": "latiendita",
  "project_path": "c:\\users\\damian\\repositorios\\latiendita",
  "project_source": "session",
  "result": "Memory saved: \"Implementado stack floci-az en LaTiendita\" (config)\n⚠ WARNING: Content contains an absolute filesystem path that may not be portable across machines. Use a repository-relative path instead.",
  "review_after": "2027-05-02 02:46:52",
  "state": "active",
  "sync_id": "obs-8ae27513adf5e966",
  "truncation": {
    "original_bytes": 868,
    "limit_bytes": 50000,
    "truncated": false
  }
}

---

## Assistant

_Thinking:_

Compose and emulator verified. Compose and emulator verified.

Listo, implementado y **verificado contra el emulador corriendo en tu máquina**.

## Archivos creados

```
LaTiendita/
├── docker-compose.yml        ✓ creado
├── infra/
│   ├── versions.tf           ✓
│   ├── providers.tf          ✓
│   ├── variables.tf          ✓
│   ├── main.tf               ✓
│   └── outputs.tf            ✓
├── scripts/trust-cert.ps1    ✓
└── README.md                 ✓ con los 6 pasos del flujo
```

## Verificación real (no teórica)

| Check | Resultado |
|---|---|
| `docker compose config --quiet` | ✅ YAML válido |
| Docker Desktop (estaba **apagado**) | ✅ lo inicié desde `%LOCALAPPDATA%\Programs\DockerDesktop` |
| `docker compose up -d` | ✅ imagen `floci/floci-az:latest` (v0.16.0) levantada |
| Healthcheck | ✅ `healthy` → `{"status":"UP","version":"0.16.0"}` |
| TLS cert endpoint | ✅ `GET /_floci/tls-cert` devuelve el PEM correcto |

## Dos cosas que salieron distinto al plan

**1. `.env.example` no se pudo crear** — los permisos del workspace bloquean escritura en archivos `.env*` (regla de protección, es acertada: ahí viven secretos). Solución elegida: el contenido completo está en el **README paso 3**, listo para copiar cuando tengas tu `.env`.

**2. `terraform` no está instalado** — ni `terraform` ni `tofu`. El `infra/` está escrito según la documentación oficial que verifiqué, pero **`terraform validate` no corrió**. Es el pendiente real: la suite CI de floci-az no cubre explícitamente `azurerm_container_app`, así que esa validación es obligatoria antes de confiar en el stack.

## Key Learnings:

1. floci-az con TLS devuelve el PEM en `/_floci/tls-cert`; sin TLS devuelve JSON (`{"tlsEnabled":false}`) — el script valida eso y falla con mensaje claro.
2. La imagen de floci-az no trae curl/wget: el healthcheck oficial es `["CMD", "healthcheck.sh"]`.
3. En Windows PowerShell 5.1 `Invoke-WebRequest` necesita `-UseBasicParsing`, y `&&` no es operador válido.
4. El FQDN del container app se enrutea con header `Host` porque todo comparte el puerto 4577.

**Pendiente**: instalar Terraform y correr `terraform init && plan` para validar el esquema de `azurerm_container_app` contra el emulador, más correr `scripts\trust-cert.ps1` (instala el cert en tu trust store de usuario — lo dejo a vos por ser un cambio de seguridad de tu máquina). ¿Instalo Terraform y valido el `infra/`?

---

## User

Que es la carpeta data ? 

---

## Assistant

_Thinking:_

User asks: what is the `data` folder. Simple answer. Note it appears in docker-compose volume `./data:/app/data`. Also check if it exists in repo (created by compose at runtime). Also should be gitignored. Quick check of the directory.

**Tool: shell**

**Input:**
```json
{
  "command": "Get-ChildItem .\\data -Recurse -Force -ErrorAction SilentlyContinue | Select-Object FullName, Length; echo \"---gitignore---\"; Get-Content .gitignore -ErrorAction SilentlyContinue",
  "workdir": "C:\\Users\\damian\\Repositorios\\LaTiendita"
}
```


FullName                                                                           Length
--------                                                                           ------
C:\Users\damian\Repositorios\LaTiendita\data\tls                                         
C:\Users\damian\Repositorios\LaTiendita\data\directory.json                        950   
C:\Users\damian\Repositorios\LaTiendita\data\entra.json                            4599  
C:\Users\damian\Repositorios\LaTiendita\data\tls\floci-az-selfsigned-ca.crt        1090  
C:\Users\damian\Repositorios\LaTiendita\data\tls\floci-az-selfsigned.crt           2489  
C:\Users\damian\Repositorios\LaTiendita\data\tls\floci-az-selfsigned.key           1675  
C:\Users\damian\Repositorios\LaTiendita\data\tls\floci-az-selfsigned.metadata.json 237   
---gitignore---




---

## Assistant

_Thinking:_

Explain what data/ is and that it needs a .gitignore entry (but we're in plan mode — can't edit). Note .gitignore is empty/missing.

Check: does the repo have a .gitignore file? Get-Content returned nothing — could be empty or missing. Check existence.

**Tool: shell**

**Input:**
```json
{
  "command": "Test-Path .gitignore; git status --short",
  "workdir": "C:\\Users\\damian\\Repositorios\\LaTiendita"
}
```

True
 M README.md
?? .agents/
?? .atl/
?? .gitignore
?? compose.yml
?? data/
?? infra/
?? scripts/
?? skills-lock.json


---

## Assistant

_Thinking:_

Interesting: git status shows `compose.yml` untracked — not `docker-compose.yml`? And `.agents/`, `skills-lock.json` — these may be OpenCode-generated. Wait, I created `docker-compose.yml`. git status shows `compose.yml`. Let me check the directory.

Also `.gitignore` is untracked and appears empty (Get-Content returned nothing).

Let me list the root.

**Tool: shell**

**Input:**
```json
{
  "command": "Get-ChildItem -Force | Select-Object Name, Length; echo \"---\"; (Get-Content .gitignore -Raw -ErrorAction SilentlyContinue) -eq $null",
  "workdir": "C:\\Users\\damian\\Repositorios\\LaTiendita"
}
```


Name             Length
----             ------
.agents                
.atl                   
.git                   
.github                
data                   
docs                   
infra                  
scripts                
services               
.gitignore       0     
compose.yml      720   
README.md        2940  
skills-lock.json 768   
---
True




---

## Assistant

_Thinking:_

The root now has `compose.yml` (720 bytes) instead of `docker-compose.yml` — someone (the user or another session) renamed/changed it. Also there's `docs/`, `services/`, `.github/` — the repo evolved since my last work (new skills, other sessions). My README.md may also have changed (2940 bytes; mine was ~2900ish — plausible it's mine).

Let me read compose.yml to answer accurately about data.

**Tool: read**

**Input:**
```json
{
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\compose.yml"
}
```

Read file C:\Users\damian\Repositorios\LaTiendita\compose.yml, lines 1-18
1: services:
2:   floci-az:
3:     image: floci/floci-az:latest
4:     ports:
5:       - "4577:4577"
6:     volumes:
7:       - /var/run/docker.sock:/var/run/docker.sock # required: Container Apps real mode
8:       - ./data:/app/data # persists state + generated TLS cert
9:     environment:
10:       FLOCI_AZ_TLS_ENABLED: "true" # required: azurerm provider discovers the cloud over HTTPS
11:       FLOCI_AZ_SERVICES_CONTAINER_APPS_MOCKED: "false" # real mode: replicas run as Docker containers
12:       FLOCI_AZ_STORAGE_MODE: hybrid # in-memory + async flush every 5s
13:     healthcheck:
14:       # The image ships no curl/wget; healthcheck.sh is the official probe.
15:       test: ["CMD", "healthcheck.sh"]
16:       interval: 5s
17:       timeout: 3s
18:       retries: 10

---

## Assistant

_Thinking:_

Explain what data/ is: host-side bind mount of /app/data inside the container, where floci-az writes:
- persisted state (hybrid storage mode flushes every 5s): directory.json, entra.json
- TLS: self-signed cert + key + CA + metadata

It's runtime state — should be gitignored (currently .gitignore is empty and data/ is untracked).

Also flag: the file is now `compose.yml` not `docker-compose.yml` (renamed at some point) and there are new dirs (.agents, services, docs, .github, skills-lock.json) — user work/other sessions; don't touch.

Note: plan mode → I can't add .gitignore entry now; propose it.

Answer concisely in Spanish.

`data/` es el **estado en disco del emulador**. Es el bind mount del volumen `./data:/app/data` del compose — lo que floci-az escribe dentro del contenedor aparece en tu repo. Lo que hay ahora mismo:

```
data/
├── directory.json                     # estado ARM: tenants, suscripciones, resource groups
├── entra.json                         # directorio Microsoft Entra (usuarios, apps, secrets)
└── tls/
    ├── floci-az-selfsigned.crt        # cert del emulador (lo usa el provider azurerm)
    ├── floci-az-selfsigned.key        # clave privada
    ├── floci-az-selfsigned-ca.crt     # CA auto-firmada
    └── floci-az-selfsigned.metadata.json
```

Funciona así:

- **`FLOCI_AZ_STORAGE_MODE: hybrid`** — todo vive en memoria pero se flushea a disco **cada 5 segundos**. Eso es lo que hace que `directory.json` y `entra.json` existan y sobrevivan reinicios del contenedor. Sin este mount, al bajar el contenedor perdés todos los resources desplegados con Terraform.
- **`tls/`** — el cert auto-firmado se genera una vez y se reutiliza. Si lo perdés, cambia y tendrías que correr `trust-cert.ps1` de nuevo (por eso se persiste: evita regenerar el cert).

## Ojo con dos cosas

**1. `data/` está sin ignorar y `.gitignore` está vacío (0 bytes).** git lo ve como `?? data/`. Ahí hay una **clave privada** (`.key`) — no debe entrar al repo. El plan sería:

```gitignore
data/
```

**2. El archivo ahora es `compose.yml`, no `docker-compose.yml`** — alguien lo renombró desde mi implementación (y aparecieron `services/`, `docs/`, `.github/`, `.agents/` que yo no toqué). Es válido: `docker compose` detecta `compose.yml` sin problema. Solo lo señalo para que sepas que hay trabajo tuyo (o de otra sesión) encima del mío y no lo piso.

Estoy en Plan mode, así que no puedo editar el `.gitignore` ahora. ¿Lo agrego cuando salgas del modo plan?

---

## User

Creame un gitignore

---

## Assistant

_Thinking:_

User asks to create a .gitignore. Skill check: none of the available skills match creating a .gitignore (documentation? no — it's not docs). Not needed.

Current .gitignore exists but is empty (0 bytes). Write it.

What to include: data/, .env, terraform artifacts (.terraform/, *.tfstate, *.tfstate.backup, crash.log, .terraform.lock.hcl? — lock file SHOULD be committed. Keep lock committed), *.tfvars? (usually contain secrets; but they're not secrets here — common practice: ignore *.tfvars, keep *.tfvars.example. Our repo has no tfvars; I'll ignore *.tfvars and *.tfvars.json but allow .example), crlf/backup, .agents/? skills-lock.json? — those are opencode artifacts; unclear if user wants them. .agents/, skills-lock.json were created by opencode/skill tooling. Include? Be careful — user may want them tracked. I'd add common tool entries but maybe not .agents (unknown). Actually .atl/ also unknown.

Keep focused: OS/editor, env secrets, Terraform, data/, floci cert artifacts (data/floci-az.crt from trust-cert.ps1 — that's scripts/floci-az.crt, ignore *.crt in scripts? The script writes floci-az.crt next to itself). Add that.

Minimal but complete:

```gitignore
# Secrets / local env
.env
.env.*
!.env.example

# floci-az runtime state (contains private key)
data/
scripts/floci-az.crt

# Terraform
.terraform/
*.tfstate
*.tfstate.*
crash.log
*.tfvars
*.tfvars.json
!*.tfvars.example
override.tf
override.tf.json
*_override.tf
*_override.tf.json

# Terraform providers/lock: keep .terraform.lock.hcl committed (deliberately not ignored)

# OS / editor
Thumbs.db
.DS_Store
*.swp
.idea/
.vscode/
```

Write it (file exists but empty — write tool overwrites). Then show git status to confirm data/ no longer untracked... note other untracked stuff (.agents, .atl, skills-lock.json, compose.yml) remains — leave.

Also mention .terraform.lock.hcl deliberately kept.

Save memory? Minor convention — maybe skip; it's a small change. The protocol says patterns/conventions → save. Eh, it's small; I'll skip to avoid noise... Actually rule: "Pattern established" — a .gitignore convention is minor. I'll skip.

**Tool: write**

**Input:**
```json
{
  "content": "# Secrets / local environment\n.env\n.env.*\n!.env.example\n\n# floci-az runtime state (includes a private key — never commit)\ndata/\nscripts/floci-az.crt\n\n# Terraform\n.terraform/\n*.tfstate\n*.tfstate.*\ncrash.log\n*.tfvars\n*.tfvars.json\n!*.tfvars.example\noverride.tf\noverride.tf.json\n*_override.tf\n*_override.tf.json\n\n# Note: .terraform.lock.hcl is intentionally committed (pinned provider versions)\n\n# OS / editor\nThumbs.db\n.DS_Store\n*.swp\n.idea/\n.vscode/\n",
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\.gitignore"
}
```

Wrote file successfully: .gitignore

---

## Assistant

_Thinking:_

Verify with git status that data/ is now ignored.

**Tool: shell**

**Input:**
```json
{
  "command": "git status --short; echo \"---\"; git check-ignore -v data/ scripts/floci-az.crt 2>$null",
  "workdir": "C:\\Users\\damian\\Repositorios\\LaTiendita"
}
```

 M README.md
?? .agents/
?? .atl/
?? .gitignore
?? compose.yml
?? infra/
?? scripts/
?? skills-lock.json
---
.gitignore:7:data/	data/
.gitignore:8:scripts/floci-az.crt	scripts/floci-az.crt


---

## Assistant

_Thinking:_

Verified: data/ ignored. scripts/ still shows untracked (dir contains trust-cert.ps1 — fine; floci-az.crt not created yet since script hasn't run).

Note: `.gitignore` itself is untracked (will be committed). The remaining untracked: .agents/, .atl/, skills-lock.json — opencode artifacts I don't decide about. Mention briefly and ask? One question rule — I can ask whether to also ignore those. Let me mention and ask one question.

Keep short.

`.gitignore` creado y verificado con `git check-ignore`:

```
.gitignore:7:data/              → data/ ignorado ✅
.gitignore:8:scripts/floci-az.crt → cert descartado ✅
```

Contenido:

- **`.env` + variantes** (con excepción para `.env.example` cuando exista)
- **`data/`** — estado de floci-az, que contiene una clave privada
- **`scripts/floci-az.crt`** — el cert que descarga `trust-cert.ps1`
- **Terraform** — `*.tfstate`, `.terraform/`, `*.tfvars` (pueden llevar secretos), crash logs. Dejé **`.terraform.lock.hcl` fuera del ignore a propósito**: ese se commitea, es lo que fija versiones de providers reproducibles. Documentado en el propio archivo.
- OS/editor básicos

Sigue sin ignorar tres cosas que no sé si querés trackear: `.agents/`, `.atl/` y `skills-lock.json` — parecen artefactos de OpenCode/herramientas, pero es tu decisión si van al repo o no. ¿Los ignoro también?
