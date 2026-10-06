# MemFixer Git Workflow & Repository Management Guide

A standardized guide for maintaining a clean, structured, and noise-free Git history for the MemFixer repository.

---

## 1. Golden Rules for Repository Hygiene

1. **Never Commit Build Artifacts or Runtime Data**:
   - Never commit `build/`, `bin/`, `.gradle/`, or `run/`.
   - The `.gitignore` file is strictly configured to exclude all compiled bytecode, runtime logs, crash reports, and local Gradle caches.
2. **Always Inspect Status Before Staging**:
   - Run `git status` before adding files to ensure no unexpected files are being tracked.
3. **Use Standardized Commit Messages**:
   - Follow the Conventional Commits specification for all commits.
   - Keep messages concise, descriptive, and written in English.
4. **No Emojis**:
   - In accordance with project operational directives, do not use emojis in commit messages or repository files.

---

## 2. Conventional Commit Standards

Every commit message follows this structure:

```
<type>(<optional scope>): <imperative description>
```

### Commit Types

| Type | Purpose | Example |
| :--- | :--- | :--- |
| **feat** | A new feature or optimization module | `feat(chunk): add zero-bit paletted storage singletons` |
| **fix** | A bug fix or crash resolution | `fix(gui): safe-check cloth-config presence before registering screen` |
| **perf** | A pure performance improvement | `perf(state): replace linear lookup with direct-mapped hash table` |
| **docs** | Documentation changes | `docs: update target platform and jar naming in architecture notes` |
| **chore** | Toolchain, gradle, or metadata updates | `chore(release): bump version to 0.1.0+mc1.21.1` |
| **refactor** | Code refactoring with unchanged behavior | `refactor(mixin): extract helper logic into dedicated utility class` |

---

## 3. Daily Development Workflow

### Step 1: Check Working Tree
Verify what files have been modified or created:

```bash
git status
```

### Step 2: Stage Intended Files
Stage specific modified files:

```bash
git add <file-path>
```

Or stage all tracked modifications:

```bash
git add .
```

### Step 3: Commit with Conventional Format
Commit your changes with a clear message:

```bash
git commit -m "feat(module): descriptive explanation of changes"
```

### Step 4: Push to Remote Repository
Push commits to the main branch:

```bash
git push origin main
```

---

## 4. Connecting to a Remote GitHub Repository (First Time Setup)

If connecting this fresh repository to a new GitHub repository:

```bash
# 1. Add remote repository URL
git remote add origin https://github.com/<username>/MemFixer.git

# 2. Rename default branch to main (if not already)
git branch -M main

# 3. Push and set upstream
git push -u origin main
```

---

## 5. Reverting or Undoing Mistakes Safely

* **Discard changes in a specific file**:
  ```bash
  git restore <file-path>
  ```
* **Unstage a file staged by mistake**:
  ```bash
  git restore --staged <file-path>
  ```
* **Review diff before committing**:
  ```bash
  git diff
  ```
