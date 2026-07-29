---
name: ci-fir-package
description: Trigger this repository's GitHub Actions Debug APK build-and-upload workflow for fir.im. Use when the user says “CI 打包并上传到 fir”, “用 CI 发布到 fir.im”, or asks to dispatch the repository's fir.im workflow and receive the Actions run URL or status.
---

# CI FIR Package

Dispatch `.github/workflows/build-and-upload-fir-im.yml` from the selected remote branch. This is the CI-only counterpart to the global `android-fir-package` skill.

## Preconditions

- Confirm the workflow commit is pushed; GitHub Actions cannot run unpushed local files.
- Confirm the GitHub CLI identity before creating a remote workflow run:

  ```bash
  gh auth status
  gh api user --jq '.login'
  ```

- Ensure the repository Secret `FIR_TOKEN` is configured. CI cannot access the local machine's `FIR_TOKEN` environment variable.

## Trigger

Use the current branch unless the user specifies a pushed branch or tag. The workflow builds Debug APKs only.

```bash
ref="$(git branch --show-current)"
gh workflow run build-and-upload-fir-im.yml --ref "$ref"
# One Debug variant:
gh workflow run build-and-upload-fir-im.yml --ref "$ref" -f variant=demoAllDebug
```

Accepted values are `demoAllDebug` and `qaHrDebug`; `demoAll` or `qaHr` automatically select their Debug build. Leave `variant` empty or use `all` to build every enabled Debug variant.

After dispatching, locate and report the run URL. Watch the run only when the user requests completion status:

```bash
gh run list --workflow build-and-upload-fir-im.yml --branch "$ref" --event workflow_dispatch --limit 1 --json databaseId,url,status,conclusion
gh run watch <run-id>
```
