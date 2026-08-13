# ghcp-poc-DtEcom

## Test execution modes

This project supports both:

- **Parallel execution**: multiple scenarios at the same time in separate browser instances
- **Sequential execution**: scenarios run one by one in a single active browser flow at a time

The concurrency is controlled by the Maven property `parallel.threads`.

### Run tests in parallel

Runs up to 4 scenarios at the same time.

```powershell
mvn test -Pparallel
```

You can also override the browser concurrency directly:

```powershell
mvn test -Dparallel.threads=4
```

### Run tests one by one

Runs the suite sequentially by limiting the TestNG/Cucumber data-provider thread count to 1.

```powershell
mvn test -Psequential
```

Or equivalently:

```powershell
mvn test -Dparallel.threads=1
```

### Default mode

If no profile is passed, the project currently defaults to:

- `parallel.threads=4`

### Cucumber report

After each run, the HTML report is generated at:

```text
target/cucumber-report.html
```

On Windows, you can open it directly with:

```powershell
Start-Process "file:///C:/AI/POC/DT_POC3/target/cucumber-report.html"
```
