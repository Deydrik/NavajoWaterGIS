# NavajoWaterGIS

An Android application for mapping and managing water well data for field technicians. Built with an offline-first architecture to support use in areas with limited connectivity.

> **Status:** On hold — active development paused. Core architecture and data layer are implemented.

---

## Screenshots

_Coming soon_

---

## Features

- View and manage water well records
- Offline-first design using local Room database
- List, detail, and add-record screens for well data
- MVVM architecture with repositories and ViewModels
- Retrofit integration prepared for remote data syncing
- Google Maps API integration for field mapping

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Architecture | MVVM |
| Networking | Retrofit |
| Local Storage | Room Database |
| Mapping | Google Maps API |
| IDE | Android Studio |

---

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- Android SDK 26+
- A Google Maps API key — get one at [Google Cloud Console](https://console.cloud.google.com/)

### Setup

1. Clone the repository
```bash
   git clone https://github.com/Deydrik/NavajoWaterGIS.git
```

2. Open the project in Android Studio

3. Add your Google Maps API key:
   - Open `local.properties` in the project root
   - Add the following line:
```
   MAPS_API_KEY=your_api_key_here
```

4. Build and run on an emulator or physical device

---

## Project Structure
```
app/
├── data/
│   ├── local/        # Room database, WellEntity, WellDao, WellDatabase
│   ├── remote/       # Retrofit setup for remote syncing
│   └── repository/   # WellRepository
├── ui/
│   ├── welllist/     # RecyclerView list of wells
│   ├── welldetail/   # Individual well detail screen
│   └── addwell/      # Add new well record screen
└── viewmodel/        # ViewModels for each screen
```

---

## Current State

The core data layer is fully implemented including `WellEntity`, `WellDao`, and `WellDatabase`. MVVM architecture is in place with repositories and ViewModels. UI screens for listing, viewing, and adding wells are designed. Retrofit is prepared but remote syncing is not yet active.

---

## Author

**Mario Smith-Pignone**
[GitHub](https://github.com/Deydrik) • [Email](mailto:pignone.mario@yahoo.com)
