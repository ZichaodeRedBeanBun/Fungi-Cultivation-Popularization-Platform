# GitHub Upload Guide - Privacy & Security 🛡️

## ⚠️ IMPORTANT: Files NOT to Upload to GitHub

This document lists all files and directories that contain sensitive information and should **NEVER** be committed to GitHub or any public repository.

---

## 🔒 Sensitive Files (Already in .gitignore)

The following files are already excluded via `.gitignore` and will not be uploaded:

### 1. Environment Configuration Files (CRITICAL)
These files may contain API keys, database credentials, backend URLs, and other secrets:

```
.env                    # Base environment variables
.env.local              # Local overrides
.env.development        # Development environment
.env.development.local  # Local development config (MOST CRITICAL)
.env.production         # Production environment
.env.staging            # Staging environment
.env.test               # Test environment
```

**Why they're sensitive:**
- Backend API URLs
- Database connection strings
- API keys and tokens
- Secret keys
- Third-party service credentials

**Action Required:**
✅ These are already in `.gitignore` - DO NOT remove them from gitignore!

### 2. Dependencies & Lock Files

```
node_modules/           # All npm packages (very large + may contain binaries)
pnpm-lock.yaml          # Exact dependency versions (optional to exclude)
package-lock.json       # npm lock file (if exists)
yarn.lock              # Yarn lock file (if exists)
```

**Why exclude:**
- `node_modules/`: Can be regenerated with `pnpm install`, very large (500MB+)
- Lock files: Optional - some teams commit them for reproducible builds

**Current status:**
- ✅ `node_modules/` is in `.gitignore`
- ⚠️ `pnpm-lock.yaml` is currently tracked - consider adding to `.gitignore` if desired

### 3. Build Output

```
dist/                   # Production build output
dist-ssr/              # SSR build output
*.local                # Any local build artifacts
report.html            # Build report
vite.config.*.timestamp* # Vite temp files
```

**Why exclude:**
- Generated files can be rebuilt
- May contain exposed configuration
- Large file sizes

**Current status:**
✅ All build outputs are in `.gitignore`

### 4. IDE & Editor Configuration

```
.idea/                 # JetBrains IDE settings (IntelliJ, WebStorm, etc.)
.vscode/               # VS Code workspace settings
*.suo                  # Visual Studio user options
*.ntvs*                # Node Tools for VS
*.njsproj              # Node.js project files
*.sln                  # Solution files
```

**Why exclude:**
- Personal editor preferences
- Local path configurations
- User-specific settings

**Current status:**
✅ `.idea/` is in `.gitignore`
⚠️ `.vscode/` is currently tracked - contains workspace recommendations (optional)

### 5. Cache & Temporary Files

```
.eslintcache           # ESLint cache
.npm-error.log*        # npm error logs
.pnpm-debug.log        # pnpm debug logs
tests/**/coverage/     # Test coverage reports
.DS_Store              # macOS folder metadata
Thumbs.db              # Windows thumbnail cache
tsconfig.tsbuildinfo   # TypeScript build info
```

**Why exclude:**
- Auto-generated caches
- System-specific files
- Can be regenerated

**Current status:**
✅ All cache files are in `.gitignore`

### 6. Logs & Debug Files

```
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
```

**Why exclude:**
- May contain sensitive data
- Environment-specific
- Large file sizes

**Current status:**
✅ Log files are in `.gitignore`

---

## ✅ Safe to Upload

The following files are safe and should be uploaded to GitHub:

### Source Code
```
src/                   # All source code
├── api/              # API service layer
├── assets/           # Static assets (images, fonts)
├── components/       # Reusable components
├── config/           # App configuration (non-sensitive)
├── directives/       # Custom directives
├── layout/           # Layout components
├── plugins/          # Vue plugins
├── router/           # Route configuration
├── store/            # State management
├── style/            # Stylesheets
├── utils/            # Utility functions
└── views/            # Page components
```

### Configuration Files (Non-Sensitive)
```
package.json          # Project dependencies
tsconfig.json         # TypeScript configuration
vite.config.ts        # Vite build configuration
eslint.config.js      # ESLint rules
stylelint.config.js   # Stylelint rules
tailwind.config.ts    # Tailwind CSS config
postcss.config.js     # PostCSS config
commitlint.config.js  # Commit lint config
```

### Documentation
```
README.md             # Project overview
ARCHITECTURE.md       # System architecture
LICENSE               # MIT License
img/                  # Screenshots and images
```

### Build Scripts & Templates
```
build/                # Build configuration scripts
mock/                 # Mock data for development
public/               # Public static assets
types/                # TypeScript type definitions
index.html            # HTML entry point
Dockerfile            # Docker build instructions
```

### Version Control
```
.gitignore            # Git ignore rules
.lintstagedrc         # Lint-staged config
.editorconfig         # Editor configuration (shared)
.browserslistrc       # Browser support targets
.markdownlint.json    # Markdown linting rules
```

---

## 📋 Pre-Upload Checklist

Before uploading to GitHub, verify the following:

### 1. Check .gitignore is Properly Configured

Run this command to see what will be committed:
```bash
git status
```

Verify these patterns are in `.gitignore`:
```gitignore
# Dependencies
node_modules/

# Environment files (CRITICAL!)
.env
.env.local
.env.*.local
.env.development.local
.env.production.local
.env.staging.local

# Build output
dist/
dist-ssr/
*.local

# IDE
.idea/
.vscode/settings.json  # Keep shared configs, ignore personal
.vscode/launch.json

# OS files
.DS_Store
Thumbs.db

# Logs
*.log
npm-debug.log*

# Cache
.eslintcache
coverage/

# Lock files (optional)
# pnpm-lock.yaml  # Uncomment if you want to exclude
```

### 2. Remove Accidentally Committed Sensitive Files

If you accidentally committed sensitive files:

```bash
# Remove from git tracking (but keep locally)
git rm --cached .env.development.local
git rm --cached .env.production
git rm --cached pnpm-lock.yaml  # optional

# Add to .gitignore
echo ".env.*.local" >> .gitignore
echo "pnpm-lock.yaml" >> .gitignore  # optional

# Commit the changes
git add .gitignore
git commit -m "chore: update gitignore to exclude sensitive files"
```

**⚠️ WARNING:** If secrets were already pushed to GitHub:
1. Rotate/change all exposed credentials immediately
2. Use `git filter-branch` or BFG Repo-Cleaner to remove from history
3. Force push (if it's your private repo)

### 3. Create Example Environment File

Create a template for other developers:

```bash
# Create example file from your local config
cp .env.development.local .env.example

# Edit to remove sensitive data
# Replace actual values with placeholders
```

Example `.env.example`:
```env
# Backend API URL (replace with your actual backend URL)
VITE_APP_BASE_API=http://localhost:8080

# Development server port
VITE_PORT=5173

# Application title
VITE_APP_TITLE=MhWeb-Admin

# Router history mode (hash or html5)
VITE_ROUTER_HISTORY=hash

# Enable/disable console dropping in production
VITE_DROP_CONSOLE=true

# Public path
VITE_PUBLIC_PATH=/
```

Commit the example file:
```bash
git add .env.example
git commit -m "docs: add example environment file"
```

### 4. Verify No Secrets in Source Code

Search for potential secrets in your code:

```bash
# Search for common secret patterns
grep -r "password" src/ --include="*.ts" --include="*.vue"
grep -r "secret" src/ --include="*.ts" --include="*.vue"
grep -r "apiKey" src/ --include="*.ts" --include="*.vue"
grep -r "token" src/ --include="*.ts" --include="*.vue" | grep -v "// " | grep "="
```

All credentials should be:
- ✅ In environment variables
- ✅ Fetched from secure backend
- ❌ NOT hardcoded in source code

### 5. Check for Hardcoded URLs

Replace hardcoded backend URLs with environment variables:

```typescript
// ❌ BAD - Hardcoded URL
const baseURL = 'http://localhost:8080';

// ✅ GOOD - Use environment variable
const baseURL = import.meta.env.VITE_APP_BASE_API;
```

### 6. Review .vscode Directory

If you want to share VS Code settings:

Keep these (safe):
```
.vscode/extensions.json    # Recommended extensions
.vscode/settings.json      # Shared workspace settings (review first)
```

Remove these (personal):
```
.vscode/launch.json        # Personal debug configs
.vscode/tasks.json         # Personal task configs
```

Add to `.gitignore`:
```gitignore
.vscode/launch.json
.vscode/tasks.json
.vscode/*.code-workspace
```

---

## 🔍 Verification Commands

### Check What Will Be Uploaded

```bash
# See all tracked files
git ls-files

# See what will be committed
git status

# Check file sizes
find . -type f -not -path './node_modules/*' -not -path './.git/*' -size +1M | sort -k5 -n

# Look for potential secrets
grep -r "AKIA[0-9A-Z]{16}" . --exclude-dir=node_modules --exclude-dir=.git  # AWS keys
grep -r "-----BEGIN.*PRIVATE KEY-----" . --exclude-dir=node_modules --exclude-dir=.git  # Private keys
```

### Clean Repository History (if needed)

If you need to remove sensitive files from git history:

```bash
# Install BFG Repo-Cleaner
# Download from: https://rtyley.github.io/bfg-repo-cleaner/

# Remove sensitive files from entire history
java -jar bfg.jar --delete-files .env.development.local my-repo.git
java -jar bfg.jar --delete-files .env.production my-repo.git

# Clean up
cd my-repo.git
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# Force push (WARNING: rewrites history)
git push --force
```

---

## 📁 Recommended .gitignore for This Project

Here's the complete `.gitignore` that should be used:

```gitignore
# Dependencies
node_modules/
.pnp
.pnp.js

# Testing
coverage/
*.lcov

# Production builds
dist/
dist-ssr/
*.local

# Environment variables - CRITICAL!
.env
.env.local
.env.*.local
.env.development
.env.development.local
.env.production
.env.production.local
.env.staging
.env.staging.local
.env.test
.env.test.local

# Logs
logs
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
pnpm-debug.log*

# Runtime data
pids
*.pid
*.seed
*.pid.lock

# Editor directories and files
.idea/
.vscode/*
!.vscode/extensions.json
!.vscode/settings.json
*.suo
*.ntvs*
*.njsproj
*.sln
*.sw?
*.code-workspace

# OS files
.DS_Store
Thumbs.db
ehthumbs.db
Desktop.ini

# TypeScript
*.tsbuildinfo
tsconfig.tsbuildinfo

# Optional npm cache directory
.npm

# Optional eslint cache
.eslintcache

# Optional REPL history
.node_repl_history

# Output of 'npm pack'
*.tgz

# Yarn Integrity file
.yarn-integrity

# parcel-bundler cache
.cache

# next.js build output
.next

# nuxt.js build output
.nuxt

# vuepress build output
.vuepress/dist

# Serverless directories
.serverless/

# FuseBox cache
.fusebox/

# DynamoDB Local files
.dynamodb/

# TernJS port file
.tern-port

# Stores VSCode versions used for testing VSCode extensions
.vscode-test

# yarn v2
.yarn/cache
.yarn/unplugged
.yarn/build-state.yml
.yarn/install-state.gz

# Report
report.html

# Vite
vite.config.*.timestamp*

# Lock files (optional - choose one approach)
# Uncomment below if you don't want to commit lock files
# pnpm-lock.yaml
# package-lock.json
# yarn.lock
```

---

## 🚀 Safe Upload Process

### Step-by-Step Guide

1. **Clone or Initialize Repository**
   ```bash
   git init
   # or
   git clone <repository-url>
   ```

2. **Ensure .gitignore is in Place**
   ```bash
   # Verify .gitignore exists and has correct content
   cat .gitignore
   ```

3. **Add Safe Files Only**
   ```bash
   git add .
   
   # Review what will be committed
   git status
   ```

4. **Verify No Sensitive Files**
   ```bash
   # Double-check no .env files are staged
   git diff --cached --name-only | grep -E "\.env|\.local"
   
   # Should return nothing
   ```

5. **Commit**
   ```bash
   git commit -m "initial commit: project setup"
   ```

6. **Create Remote Repository on GitHub**
   - Go to github.com
   - Create new repository
   - DO NOT initialize with README (we already have one)

7. **Push to GitHub**
   ```bash
   git remote add origin https://github.com/yourusername/mh-web-admin.git
   git branch -M main
   git push -u origin main
   ```

8. **Verify on GitHub**
   - Visit your repository on GitHub
   - Check that no sensitive files are visible
   - Verify .env files are NOT in the file list

---

## 🛡️ Security Best Practices

### 1. Never Commit Secrets
- API keys
- Database passwords
- JWT secrets
- OAuth credentials
- Private keys
- Access tokens

### 2. Use Environment Variables
```typescript
// ✅ Correct way
const apiKey = import.meta.env.VITE_API_KEY;

// ❌ Wrong way
const apiKey = "sk-1234567890abcdef";
```

### 3. Rotate Exposed Credentials
If you accidentally commit secrets:
1. Immediately revoke/change the credential
2. Remove file from git history
3. Audit for unauthorized access
4. Update documentation

### 4. Use .env.example
Always provide a template:
```bash
.env.example          # ✅ Commit this
.env.development.local # ❌ Never commit
```

### 5. Regular Audits
Periodically check:
```bash
# Scan for secrets
git log --all -p | grep -i "password\|secret\|key\|token"

# Check for large files
git rev-list --objects --all | \
  git cat-file --batch-check='%(objecttype) %(objectname) %(objectsize) %(rest)' | \
  sed -n 's/^blob //p' | \
  sort -n -k 2 | \
  tail -n 10
```

---

## 📞 Need Help?

If you accidentally committed sensitive data:

1. **Don't Panic** - It happens to everyone
2. **Rotate Credentials** - Change all exposed secrets immediately
3. **Remove from History** - Use BFG Repo-Cleaner or git filter-branch
4. **Learn** - Review this guide to prevent future issues

Resources:
- [GitHub Help - Remove Sensitive Data](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)
- [BFG Repo-Cleaner](https://rtyley.github.io/bfg-repo-cleaner/)
- [Git Filter-Branch](https://git-scm.com/docs/git-filter-branch)

---

## ✅ Final Checklist Before Publishing

- [ ] `.env.*.local` files are in `.gitignore`
- [ ] `node_modules/` is in `.gitignore`
- [ ] `dist/` is in `.gitignore`
- [ ] No hardcoded secrets in source code
- [ ] `.env.example` file created with placeholders
- [ ] Verified with `git status` - no sensitive files staged
- [ ] Checked file sizes - no unexpectedly large files
- [ ] Reviewed `.vscode/` settings
- [ ] Scanned for potential secrets with grep
- [ ] Tested fresh clone works without .env files
- [ ] README.md references ARCHITECTURE.md
- [ ] LICENSE file is present
- [ ] All documentation is complete

---

**Remember:** Once something is pushed to GitHub, even if deleted later, it may still exist in the git history. Always double-check before pushing!

**Last Updated:** 2026-06-06
