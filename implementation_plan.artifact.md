# Car Service App Implementation Plan

This plan outlines the creation of a cross-platform (Android & Web) application for tracking car service records, inspired by the existing Laravel project. We will use **Ionic + Vue + Capacitor** to achieve a native-like experience on Android and a functional web version.

## User Review Required

> [!IMPORTANT]
> The app will use **Capacitor SQLite** for local persistence. For the web version, it will fallback to a web-based SQLite or IndexedDB. Data synchronization between devices is not included in this initial plan but can be added later (e.g., via Firebase).

## Proposed Changes

### 1. Project Initialization
- Initialize a new Ionic Vue project using Vite.
- Set up Capacitor and add the Android platform.
- Install necessary dependencies: `@ionic/vue`, `@ionic/vue-router`, `@capacitor/sqlite`, `lucide-vue-next` (for icons).

### 2. Data Models & Storage
- **Car**: `id`, `make`, `model`, `year`, `license_plate`, `vin`, `notes`.
- **ServiceEntry**: `id`, `car_id`, `date`, `kilometers`, `service_name`, `operations` (tags), `cost`.
- **MaintenanceInterval**: `id`, `car_id`, `component`, `interval_km`, `interval_years`.
- Create a `database` service to handle CRUD operations via SQLite.

### 3. UI Components (Ionic + Vue)
- **Dashboard**: Overview of cars and upcoming maintenance.
- **Car Management**:
    - List of cars.
    - Add/Edit Car form.
- **Service Records**:
    - List of records for a specific car.
    - Add/Edit Service Entry form with a "tag-like" input for operations (auto-suggesting from previous entries).
- **Maintenance Intervals**:
    - Manage intervals per car.
    - Logic to calculate and display the next scheduled service.
- **Search**: Global or per-car search for entries and operations.

### 4. Tagging System Logic
- Store a distinct list of used "operations" in the database.
- Provide a `datalist` or custom autocomplete for the "operations" field in the service entry form.

### 5. Maintenance Interval Calculation
- For each interval (e.g., "Oil Change", 10k km / 1 year):
    - Find the latest service entry for that car that includes this "component" in its operations.
    - Next KM = Last KM + Interval KM.
    - Next Date = Last Date + Interval Years.
    - Compare with current status to show "Due in X km" or "Overdue".

## Verification Plan

### Automated Tests
- Unit tests for the maintenance interval calculation logic.
- Integration tests for database CRUD operations.

### Manual Verification
- Deploy to Android Emulator/Device and verify native functionality (SQLite, UI responsiveness).
- Run as a web app (`npm run dev`) and verify functionality in the browser.
- Test the tagging auto-suggestion by adding multiple entries.
