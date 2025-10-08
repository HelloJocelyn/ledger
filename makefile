up:
  docker-compose -f infra/docker-compose.yml up -d
down:
  docker-compose -f infra/docker-compose.yml down
backend:
  ./gradlew :backend:payment-api:bootRun
frontend:
  npm --prefix frontend/dashboard run dev
