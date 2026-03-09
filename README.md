# Docker Training

A hands-on Docker training repository with a GitHub Actions CI workflow.

## What's included

- **Dockerfile** — minimal Alpine-based image
- **.github/workflows/ci.yml** — CI pipeline that builds and tests the Docker image on every push/PR to `main`

## GitHub Actions Workflow

The workflow triggers on every push or pull request to `main` and:
1. Checks out the code
2. Sets up Docker Buildx
3. Builds the Docker image
4. Runs a smoke test on the container
5. Lists available Docker images
