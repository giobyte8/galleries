# CRUD for scan schedule

We're gonna allow to create 'scan schedule' entries for some directories.
This requirements will keep focused on implementing the CRUD part of it.

## Storage requirements

New Node to store in Neo4j, use below entity for it and declare
relationships using Spring Data Neo4j annotations

- Entity for node `ScanSchedule`:
  - `UUID id`: unique identifier for the scan schedule (primary key)
  - `String schedule`: Cron expression for the scan schedule
  - `String tzOffset`: Timezone offset for the schedule
  - `boolean enabled`: Indicate if the scan schedule is active
  - `created_at`: timestamp, when the scan schedule was created
  - `updated_at`: timestamp, when the scan schedule was last updated
- Graph relationship:
  - `ScanSchedule` associated to `Directory` via 'SCANS' relationship.

## CRUD UI
New page in the UI to manage `scan schedules`.

- Create: Form for a new scan schedule for a directory.
  - Users specify: Directory, cron expression, timezone offset, and whether
    the schedule is enabled.
  - Directory selection is done via an autocomplete input that queries by
    directory path.
  - Cron expression: Just use a plain text input for it. 
  - Timezone offset is populated with user's local timezone by default,
    but can be adjusted.
- Read: Lists existing schedules using a list of cards.
  - Paginated by default.
  - Don't use tables, but cards with details of each schedule.
- Update: Edit existing schedules via an edit button on each card.
  - Opens a form pre-filled with the schedule's current details.
  - Users can modify the cron expression, timezone offset, and enabled status.
