# Admin Dashboard

We need a web dashboard for admin users to manage the system.
This dashboard should allow admins to:

- List all the scan operations and their stats (In a paginated view)
- List existing root directories (In a paginated view)
- Trigger a scan for any root directory directly from the UI

## UI Functional Requirements

- The dashboard should be responsive and user-friendly.
- It should use Bootstrap components for styling and layout as much as
  possible, ensuring a consistent look and feel across different devices.
- It should provide clear navigation to main sections:
  - Galleries: Lists existing root directories
  - Scans: Lists past scan operations

### Galleries Section

Display a paginated list/table of root directories using
`DirectoryRepository#findRoots`, showing:

- **Directory path** (monospaced font for readability)
- **Recursive flag** (Yes/No or icon)
- **Status** (color-coded badge: `SCAN_PENDING`, `SCAN_IN_PROGRESS`, `SCAN_COMPLETED`, `SCAN_FAILED`)
- **Action: Scan now button**
  - Clicking this button should trigger a scan request for the directory
  - Show a loading spinner while the request is in flight (no page reload)
  - Display a brief feedback message via toast: success ("Scan queued") or error
  - Use HTMX to POST the request asynchronously
  - Backend behavior for trigger endpoint:
    - Load the target directory by id; if not found, return an error fragment
    - If directory status is `SCAN_IN_PROGRESS`, do not enqueue a new scan and
      return an "already in progress" feedback fragment
    - Build a `ScanRequest` instance using the existing class
    - Populate at least: generated UUID `id`, target directory `path`, and
      `requestedAt` timestamp
    - Publish the request to RabbitMQ using injected `AmqpTemplate`
      (same integration style used by `RemoteThumbnailsService`)
  - Trigger endpoint response contract (toast content fragment):
    - Success: "Scan queued" message
    - Not found: "Directory not found" message
    - Conflict: "Scan already in progress" message
    - Publish failure: "Unable to queue scan" message

### Scans Section

Display a paginated list/table of scan operations (`ScanStats` entity/node),
sorted descendingly by `startedAt` date:

- **Directory path** (the directory that was scanned; from `ScanStats.path`)
  - Path should be clickable
  - Clicking a path applies filtering so only scans for that path are shown
  - Filtering is passed via query params using the corresponding directory path
    (e.g., `?path=cameras/iPhone`)
  - Path values in query params must be URL-encoded
  - Include a clear "reset filter" action to return to unfiltered results
- **Started At** (human-readable timestamp, e.g., "Mar 19, 10:15 AM")
- **Duration** (human-readable format, e.g., "2m 34s")
  - For in-progress scans (status = `IN_PROGRESS`), display elapsed time from
    `startedAt` to current time and an "In progress" note with a spinner icon
    to let the user know the scan is still running
  - For completed scans, calculate from `startedAt` to `completedAt`
- **Stats** (compact multi-line (<ul>) summary):
  - Images: Unchanged / New / Updated / Not Found
  - Directories: Found
- **Status** (color-coded badge: `IN_PROGRESS`, `COMPLETED`, `FAILED`)
  - Note: this status is from `ScanStats` and is different from directory status
    (`SCAN_PENDING`, `SCAN_IN_PROGRESS`, `SCAN_COMPLETED`, `SCAN_FAILED`)
- **Polling/Auto-refresh**:
  - The table content should auto-refresh every 3 seconds to reflect the latest
    scan states
  - Polling must preserve current query state (`path`, `page`, `size`, `sort`)
    so the user view does not reset during refresh
  - In-progress scans should show live-updating counters and elapsed time
  - Completed/failed scans are still polled, but their values are expected to
    remain stable after completion

## Frontend Interactivity

To achieve real-time updates and dynamic actions without a JavaScript build
pipeline, use:

### HTMX (Declarative server interactions via HTML attributes)

- **Auto-polling**: The scans table container uses `hx-trigger="every 3s"`
  to poll the server every 3 seconds and swap in fresh table content
- **Trigger scan button**: Each row in the Galleries table has a "Scan now"
  button that uses `hx-post` to send a scan request asynchronously
- **Feedback messages**: The server returns a small HTML snippet (toast content)
  that HTMX swaps into a toast container
- **Loading state**: HTMX automatically shows a loading indicator while the
  request is in flight (customize via CSS class `htmx-request`)
- **Path filtering**: Clicking a directory path in the scans table should use
  `hx-get` with query params (e.g., `path=<dirPath>`) and refresh the scans
  table container fragment
- **State preservation**: HTMX requests for polling, pagination, and filtering
  must keep current query params (`path`, `page`, `size`, `sort`)

### Alpine.js (Lightweight client-side state and interactivity)

- **Expandable stats**: Use Alpine `x-show` to expand/collapse the Stats cell
  to show full details
- **Theme toggle**: Store light/dark theme preference in `localStorage` and
  apply to the entire page
- **Modals or toasts**: If needed, use Alpine for simple confirmation dialogs or
  toast notifications

### Integration

Both libraries load via CDN and integrate seamlessly with Thymeleaf:
- Add HTMX and Alpine script tags to the base template
- Add Bootstrap JS bundle via CDN (required for toast behavior)
- Use `hx-*` attributes in Thymeleaf `<button>` and `<div>` elements
- Use `x-data` and `x-on:*` for Alpine interactivity
- No build step, transpiler, or npm required

## Technical Requirements

### Dependencies

Based on current `build.gradle`, most required backend capabilities are already
present:

- `spring-boot-starter-web` (already present)
- `spring-boot-starter-amqp` (already present; needed for scan request publish)

Add the missing dependency:

- `spring-boot-starter-thymeleaf` (required for server-side template rendering)

No additional frontend build dependencies are required.
Bootstrap, HTMX, and Alpine.js should be loaded via CDN in Thymeleaf templates.

Security and authentication will be handled in future iterations; the
dashboard is currently unsecured for development.

### Controller Routes

Organize controllers into two groups:

#### Main Views (`/admin/**`)

- `GET /admin` -> Dashboard home / redirect to galleries
- `GET /admin/galleries` -> Galleries listing page (full HTML)
- `GET /admin/scans` -> Scans listing page (full HTML)
  - Pagination should be handled through Spring `Pageable` in controller method
    signatures (no manual pagination-parameter parsing)
  - Supports optional filtering query param: `path`
  - Default sort for scans must be `startedAt` descending

#### Fragments (`/admin/fragments/**`)

These endpoints return partial HTML for HTMX polling and dynamic updates:

- `GET /admin/fragments/galleries-table` -> Returns the full Galleries table
  container fragment (table + embedded pagination controls), paginated via
  Spring `Pageable`
- `GET /admin/fragments/scans-table` -> Returns the full Scans table container
  fragment (table + embedded pagination controls), paginated via Spring
  `Pageable`, optional filtering by path
- `POST /admin/fragments/directories/{id}/trigger-scan` -> Loads directory,
  validates status, builds a `ScanRequest`, and publishes it to RabbitMQ using
  injected `AmqpTemplate`; returns toast content fragment for
  success/error states

### Data Pagination

- **Galleries table**: Use `DirectoryRepository#findRoots(Pageable)`
- **Scans table**: `ScanStatsRepository` should support Spring Data pagination,
  optional filtering by path, and sorting by `startedAt` descending

Controllers should accept Spring `Pageable` directly and delegate pagination to
repository queries instead of manually processing pagination-related arguments.

## Code Structure

- Create a new Java package `me.giobyte8.galleries.admin` for all admin-ui-related
  code
- Java package structure:
  - `admin.controllers.DashboardController` -> handles `/admin/**` routes
  - `admin.controllers.FragmentsController` -> handles `/admin/fragments/**`
    routes
  - `admin.services.ScanRequestsService` -> encapsulates admin scan trigger
    logic: load directory, validate status, build `ScanRequest`, publish via
    `AmqpTemplate`, and return UI feedback result

- Thymeleaf templates location (resources, not Java package):
  - `src/main/resources/templates/admin/layout.html`
  - `src/main/resources/templates/admin/galleries.html`
  - `src/main/resources/templates/admin/scans.html`
  - `src/main/resources/templates/admin/fragments/galleries-table.html`
  - `src/main/resources/templates/admin/fragments/scans-table.html`
  - `src/main/resources/templates/admin/fragments/trigger-scan-feedback.html`
  - Note: pagination controls are embedded inside `galleries-table.html` and
    `scans-table.html` fragments (no standalone pagination fragment templates)

- Leverage existing repositories (`DirectoryRepository`, `ScanStatsRepository`)
  but keep admin-specific business logic (e.g., pagination,
  filtering, scan publishing) within the `admin` package
- Follow Spring Boot best practices: thin controllers, service layer for
  business logic

## Styling and UX

- Use Bootstrap 5 components: tables, badges (for status), spinners, alerts
- Color-code status badges:
  - `SCAN_PENDING` → Gray
  - `SCAN_IN_PROGRESS` → Blue (with spinner icon)
  - `COMPLETED` → Green
  - `FAILED` → Red
- Use `<code>` or `.font-monospace` for directory paths
- Pagination: Use Bootstrap pagination component (HTMX-aware `hx-get` on page
  links)
- Light/dark theme: Auto-detect system preference and allow manual toggle via
  Alpine
- Loading states: HTMX-triggered requests show a spinner overlay or fade effect
  (via `.htmx-request` CSS class styling)
