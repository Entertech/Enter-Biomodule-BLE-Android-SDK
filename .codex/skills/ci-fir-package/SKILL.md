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

Before artifact upload and fir.im publication, the workflow reads every APK's package name and `versionCode`, then compares it with the latest fir.im build for that Android package. It fails when the local build is less than or equal to the platform build; a missing fir.im package is allowed as its first upload.

After dispatching, locate and report the run URL. Watch the run only when the user requests completion status:

```bash
gh run list --workflow build-and-upload-fir-im.yml --branch "$ref" --event workflow_dispatch --limit 1 --json databaseId,url,status,conclusion
gh run watch <run-id>
```

## Failure Reporting

If the run fails, report its URL, branch/ref, requested variant, failed job and step, then retrieve the exact failed log before summarizing the cause:

```bash
gh run view <run-id> --json url,status,conclusion,jobs
gh run view <run-id> --log-failed
```

Classify the failure as Gradle build, Debug APK discovery/artifact, missing `FIR_TOKEN` repository Secret, build-number precheck, or fir.im upload. For build-number failures, include the package name and the local versus platform build values. Include the relevant error text and APK path when present; never reveal the secret value or claim that a failed run uploaded every APK.
