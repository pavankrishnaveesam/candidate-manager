# API Reference

Base URL: `http://localhost:8080/api/v1`

## Candidate payload

```json
{
  "fullName": "Sophia Chen",
  "currentRole": "Senior Product Designer",
  "email": "sophia.chen@example.com",
  "phone": "+1 415 555 0101",
  "skills": "UI/UX, Figma, User Research",
  "experienceYears": 7,
  "location": "San Francisco",
  "status": "INTERVIEW"
}
```

`fullName`, `currentRole`, `email`, and `experienceYears` are required. If `status` is omitted while creating a candidate, it defaults to `APPLIED`.

## Endpoints

| Method | Path | Result |
| --- | --- | --- |
| POST | `/candidates` | Creates a candidate and returns `201 Created`. |
| GET | `/candidates/{id}` | Returns one candidate. |
| PUT | `/candidates/{id}` | Replaces editable candidate details. |
| DELETE | `/candidates/{id}` | Deletes a candidate and returns `204 No Content`. |
| GET | `/candidates` | Returns a stable paginated response. |

Filters combine with `AND`; repeated `skill` values combine with `OR`. Valid status values are `APPLIED`, `SCREENING`, `INTERVIEW`, `OFFERED`, `HIRED`, and `REJECTED`.

```text
GET /candidates?q=sophia&skill=figma&skill=research&minExperience=5&location=San%20Francisco&status=INTERVIEW&page=0&size=20&sort=createdAt,desc
```

Validation and request errors return `{ timestamp, status, message, fieldErrors }` with HTTP `400`; missing candidates return `404`; duplicate emails return `409`.
