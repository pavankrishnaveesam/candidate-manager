# AI-Assisted Development

AI was used to discuss the API shape, generate an initial implementation, draft tests, and review the dashboard-driven filtering flow.

The generated output was validated by compiling the project and running the JUnit and MockMvc test suite. The API design was adjusted after review: request DTOs enforce required experience and phone format, missing create status defaults to `APPLIED`, repeated skill filters use OR semantics, test data uses isolated H2, and pagination uses a dedicated response record.

AI suggestions were not accepted blindly. Avatar URLs and match scores were excluded because the product has no source for avatar data or a defined scoring algorithm. Skills remain a text field instead of a separate relation because the assignment needs straightforward candidate filtering rather than a complex taxonomy.
