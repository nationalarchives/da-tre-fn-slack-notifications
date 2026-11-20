FROM  sbtscala/scala-sbt:eclipse-temurin-21.0.6_7_1.10.10_3.6.3 AS builder
COPY . /lambda/src/
WORKDIR /lambda/src/
RUN sbt assembly

FROM public.ecr.aws/lambda/java:21
RUN dnf install -y rpm-4.16.1.3-29.amzn2023.0.10 rpm-libs-4.16.1.3-29.amzn2023.0.10 --releasever=latest && dnf clean all
COPY --from=builder /lambda/src/target/function.jar ${LAMBDA_TASK_ROOT}/lib/
CMD ["uk.gov.nationalarchives.tre.LambdaHandler::handleRequest"]
