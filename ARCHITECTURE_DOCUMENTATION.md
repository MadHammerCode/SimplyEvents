# SimplyEvents - Architektur & Code-Dokumentation

## Inhaltsverzeichnis
1. [Projektübersicht](#1-projektübersicht)
2. [Architektur-Überblick](#2-architektur-überblick)
3. [Schichten-Architektur](#3-schichten-architektur)
4. [REST-Controller & Endpoints](#4-rest-controller--endpoints)
5. [Services (Business Logic)](#5-services-business-logic)
6. [Domain-Modelle](#6-domain-modelle)
7. [Persistence Layer](#7-persistence-layer)
8. [Request-Flow Beispiele](#8-request-flow-beispiele)
9. [Billing Modul](#9-billing-modul)
10. [Sicherheit & Authentifizierung](#10-sicherheit--authentifizierung)
11. [Häufige Fragen (FAQ)](#11-häufige-fragen-faq)

---

## 1. Projektübersicht

### Was ist SimplyEvents?
SimplyEvents ist eine **Event-Management-Plattform**, die es ermöglicht:
- Events zu erstellen und zu verwalten (Vendors)
- Events zu buchen (Customers)
- Teilnehmer einzuchecken (Frontoffice)
- Rechnungen zu erstellen (Billing)
- Benutzer zu verwalten (Admin)

### Technologie-Stack
| Komponente | Technologie |
|------------|-------------|
| **Backend** | Java 21, Spring Boot 3.5.6 |
| **Datenbank** | PostgreSQL |
| **ORM** | Spring Data JPA / Hibernate |
| **Security** | Spring Security (Session-basiert) |
| **Build** | Gradle |
| **Frontend** | Thymeleaf + JavaScript |
| **PDF-Generierung** | iText 8.0.2 |
| **Container** | Docker & Docker Compose |

### Projektstruktur
```
src/main/java/at/fhv/simplyevents/
├── EventApplication.java          # Spring Boot Entry Point
├── config/                         # Konfigurationsklassen
├── rest/                           # REST Controller (HTTP-Schicht)
│   └── dto/                        # Data Transfer Objects
├── application/                    # Use Case Interfaces
│   └── port/in/                    # Input Ports (Use Cases)
├── service/                        # Service-Implementierungen
├── domain/                         # Domain Layer
│   ├── model/                      # Domain-Modelle
│   └── repository/                 # Repository Ports (Interfaces)
├── infrastructure/                 # Infrastruktur
│   └── jpa/adapter/                # Repository-Adapter
├── persistence/                    # Persistenz-Schicht
│   ├── springdata/                 # Spring Data Repositories
│   ├── mapper/                     # Entity ↔ Domain Mapper
│   └── model/                      # JPA Entities
└── billing/                        # Billing Bounded Context
```

---

## 2. Architektur-Überblick

### Hexagonale Architektur (Ports & Adapters)

SimplyEvents verwendet die **Hexagonale Architektur**, auch bekannt als "Ports & Adapters":

```
                    ┌─────────────────────────────────────┐
                    │           HTTP Request              │
                    └─────────────────┬───────────────────┘
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  ADAPTERS (Eingang)                                                      │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  REST Controller (EventRestController, BookingRestController)    │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────┬───────────────────────────────────┘
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  PORTS (Eingang) - Use Case Interfaces                                   │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  EventUseCase, BookingUseCase, AuthUseCase, UserUseCase, ...     │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────┬───────────────────────────────────┘
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  APPLICATION LAYER - Services                                            │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  EventService, BookingService, AuthService, UserService, ...     │    │
│  │  (implementieren die Use Case Interfaces)                        │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────┬───────────────────────────────────┘
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  DOMAIN LAYER                                                            │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Domain Models: Event, User, ActiveBooking, Participant, ...     │    │
│  │  (Reine Business-Logik, keine Framework-Abhängigkeiten)          │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────┬───────────────────────────────────┘
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  PORTS (Ausgang) - Repository Interfaces                                 │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  EventRepositoryPort, UserRepositoryPort, BookingRepositoryPort  │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────┬───────────────────────────────────┘
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  ADAPTERS (Ausgang) - Repository Implementations                         │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  EventRepositoryAdapter, UserRepositoryAdapter, ...              │    │
│  │  (verwenden Spring Data JPA Repositories)                        │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────┬───────────────────────────────────┘
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  INFRASTRUKTUR                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Spring Data JPA Repositories + JPA Entities + Mapper            │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────┬───────────────────────────────────┘
                                      ▼
                    ┌─────────────────────────────────────┐
                    │           PostgreSQL DB             │
                    └─────────────────────────────────────┘
```

### Warum diese Architektur?

1. **Testbarkeit**: Services können mit Mock-Repositories getestet werden
2. **Austauschbarkeit**: Datenbank kann gewechselt werden ohne Business-Logik zu ändern
3. **Saubere Trennung**: Domain-Logik ist frei von Framework-Code
4. **Wartbarkeit**: Klare Grenzen zwischen Schichten

---

## 3. Schichten-Architektur

### Übersicht der Schichten

| Schicht | Paket | Verantwortung |
|---------|-------|---------------|
| **REST** | `/rest/` | HTTP-Requests empfangen, Validierung, Response |
| **Application** | `/application/port/in/` | Use Case Definitionen (Interfaces) |
| **Service** | `/service/` | Business-Logik, Orchestrierung |
| **Domain** | `/domain/model/` | Geschäftsobjekte, Regeln |
| **Repository Port** | `/domain/repository/` | Persistenz-Interfaces |
| **Repository Adapter** | `/infrastructure/jpa/adapter/` | Implementierung der Ports |
| **JPA** | `/persistence/` | Entities, Mapper, Spring Data Repos |

### Abhängigkeitsfluss (Dependency Rule)

```
REST Controller
      │
      ▼ ruft auf
Use Case Interface (z.B. EventUseCase)
      │
      ▼ implementiert von
Service (z.B. EventService)
      │
      ▼ verwendet
Repository Port Interface (z.B. EventRepositoryPort)
      │
      ▼ implementiert von
Repository Adapter (z.B. EventRepositoryAdapter)
      │
      ▼ delegiert an
Spring Data JPA Repository (z.B. EventJpaRepository)
      │
      ▼
Datenbank
```

**Wichtig**: Abhängigkeiten zeigen immer nach innen (zur Domain). Die Domain hat keine Abhängigkeiten zu äußeren Schichten.

---

## 4. REST-Controller & Endpoints

### Alle Controller im Überblick

| Controller | Basis-URL | Zweck |
|------------|-----------|-------|
| `EventRestController` | `/api/events` | Event-Verwaltung |
| `BookingRestController` | `/api/bookings` | Buchungen |
| `AuthController` | `/api/auth` | Authentifizierung |
| `UserController` | `/api/users` | Benutzerprofil |
| `WishlistRestController` | `/api/wishlist` | Wunschliste |
| `CheckInController` | `/api/checkin` | Teilnehmer-Check-In |
| `PaymentController` | `/api/payments` | Zahlungsabwicklung |
| `AdminRestController` | `/api/admin` | Admin-Funktionen |
| `InvoiceController` | `/api/invoices` | Rechnungen (Billing) |

---

### EventRestController (`/api/events`)

**Zweck**: Verwaltung von Events (Erstellen, Bearbeiten, Veröffentlichen)

| Methode | Endpoint | Beschreibung | Wer darf? |
|---------|----------|--------------|-----------|
| `POST` | `/api/events` | Event erstellen | VENDOR, BACKOFFICE |
| `GET` | `/api/events` | Öffentliche Events abrufen | Alle |
| `GET` | `/api/events/backoffice` | Alle Events (mit Filter) | BACKOFFICE |
| `GET` | `/api/events/{id}` | Einzelnes Event | Alle |
| `PUT` | `/api/events/{id}` | Event aktualisieren | VENDOR, BACKOFFICE |
| `DELETE` | `/api/events/{id}` | Event löschen | VENDOR, BACKOFFICE |
| `POST` | `/api/events/{id}/publish` | Event veröffentlichen | VENDOR, BACKOFFICE |
| `POST` | `/api/events/{id}/toggle-cancel` | Event stornieren/reaktivieren | BACKOFFICE |

**Code-Beispiel** (vereinfacht):
```java
@RestController
@RequestMapping("/api/events")
public class EventRestController {

    private final EventUseCase eventUseCase;  // ← Interface, nicht Service direkt

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody CreateEventRequest request) {
        // 1. Request validieren
        // 2. In Command umwandeln
        CreateEventCommand command = new CreateEventCommand(...);
        // 3. Use Case aufrufen
        EventResult result = eventUseCase.createEvent(command);
        // 4. Response zurückgeben
        return ResponseEntity.ok(toResponse(result));
    }
}
```

---

### BookingRestController (`/api/bookings`)

**Zweck**: Buchungsverwaltung für Kunden

| Methode | Endpoint | Beschreibung |
|---------|----------|--------------|
| `POST` | `/api/bookings` | Neue Buchung erstellen (pending) |
| `POST` | `/api/bookings/confirm` | Buchung bestätigen |
| `POST` | `/api/bookings/cancel` | Buchung stornieren |
| `GET` | `/api/bookings/{id}` | Buchung abrufen |
| `GET` | `/api/bookings/my/bookings` | Meine Buchungen |

**Buchungsablauf**:
```
1. POST /api/bookings         → PendingBooking (im Cache, 15 Min gültig)
2. POST /api/bookings/confirm → ActiveBooking (in DB gespeichert)
   oder
   Timeout (15 Min)           → Buchung verfällt
```

---

### AuthController (`/api/auth`)

**Zweck**: Benutzerregistrierung und Login

| Methode | Endpoint | Beschreibung |
|---------|----------|--------------|
| `POST` | `/api/auth/register/customer` | Kunde registrieren |
| `POST` | `/api/auth/register/vendor` | Vendor registrieren |
| `POST` | `/api/auth/login` | Einloggen |

**Login-Ablauf**:
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginDTO dto, HttpServletRequest request) {
    // 1. Credentials prüfen
    User u = auth.login(new LoginCommand(dto.email(), dto.password()));

    // 2. Spring Security Context setzen
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // 3. Session erstellen
    HttpSession session = request.getSession(true);
    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

    return ResponseEntity.ok(toDto(u));
}
```

---

### CheckInController (`/api/checkin`)

**Zweck**: Frontoffice-Funktionen für Event-Tag

| Methode | Endpoint | Beschreibung |
|---------|----------|--------------|
| `GET` | `/api/checkin/event/{eventId}/participants` | Teilnehmerliste |
| `GET` | `/api/checkin/event/{eventId}/bookings` | Buchungen zum Event |
| `PUT` | `/api/checkin/participants/{id}` | Check-In Status ändern |
| `POST` | `/api/checkin/event/{eventId}/bookings` | Walk-In Buchung |
| `DELETE` | `/api/checkin/bookings/{id}` | Buchung löschen |

---

### PaymentController (`/api/payments`)

**Zweck**: Zahlungsintegration mit externem MockPay-Service

| Methode | Endpoint | Beschreibung |
|---------|----------|--------------|
| `POST` | `/api/payments/start/{bookingId}` | Zahlung starten |
| `POST/GET` | `/api/payments/callback` | Callback von MockPay |

**Zahlungsablauf**:
```
1. Frontend ruft /api/payments/start/{bookingId}
2. Backend erstellt Payment bei MockPay (externer Service)
3. MockPay gibt Redirect-URL zurück
4. User wird zu MockPay weitergeleitet
5. Nach Zahlung: MockPay ruft /api/payments/callback
6. Backend aktualisiert Buchungsstatus (PAID/PAYMENT_FAILED)
7. User wird zu Erfolgs-/Fehlerseite weitergeleitet
```

---

## 5. Services (Business Logic)

### Übersicht aller Services

| Service | Implements | Verantwortung |
|---------|------------|---------------|
| `EventService` | `EventUseCase` | Event-CRUD, Status-Verwaltung |
| `BookingService` | `BookingUseCase` | Buchungen erstellen/stornieren |
| `AuthService` | `AuthUseCase` | Registrierung, Login |
| `UserService` | `UserUseCase` | Profilverwaltung |
| `CheckInService` | `CheckInUseCase` | Teilnehmer-Management |
| `PaymentService` | `PaymentUseCase` | Zahlungsintegration |
| `WishlistService` | `WishlistUseCase` | Wunschliste |

---

### EventService

**Verantwortlichkeiten**:
- Event erstellen (mit/ohne Bild)
- Event aktualisieren
- Event veröffentlichen (DRAFT → PUBLISHED)
- Event stornieren/reaktivieren
- Kapazitätsverwaltung (`availableSlots`)

**Injizierte Dependencies**:
```java
@Service
public class EventService implements EventUseCase {
    private final EventRepositoryPort eventRepository;   // ← Port, nicht JPA Repo
    private final FileStoragePort fileStorage;           // ← Port für Dateispeicher

    // Konstruktor-Injection (empfohlen)
    public EventService(EventRepositoryPort eventRepository, FileStoragePort fileStorage) {
        this.eventRepository = eventRepository;
        this.fileStorage = fileStorage;
    }
}
```

**Wichtige Methoden**:
```java
public EventResult createEvent(CreateEventCommand cmd) {
    // 1. Domain-Objekt erstellen
    Event event = Event.createDraft();
    event.applyDetails(cmd.title(), cmd.category(), cmd.price(), ...);

    // 2. Persistieren (via Port)
    Event saved = eventRepository.save(event);

    // 3. Result zurückgeben
    return toResult(saved);
}

public EventResult publishEvent(Long eventId) {
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> NotFoundException.forEntity("Event", eventId));

    event.setStatus(EventStatus.PUBLISHED);  // Domain-Logik

    return toResult(eventRepository.save(event));
}
```

---

### BookingService

**Buchungs-Lebenszyklus**:
```
PENDING (Cache, 15 Min)
    │
    ├─── confirm ───► CONFIRMED → PENDING_PAYMENT → PAID
    │                                    │
    │                                    └──► PAYMENT_FAILED
    └─── timeout ───► (verfällt)

ACTIVE ───► cancel ───► CANCELLED (CancelledBooking)
```

**Kapazitätsprüfung**:
```java
public PendingBookingResponse createBooking(CreateBookingCommand cmd) {
    Event event = eventRepository.findById(cmd.eventId())
        .orElseThrow(() -> NotFoundException.forEntity("Event", cmd.eventId()));

    // Bereits gebuchte Plätze zählen
    int alreadyBooked = activeBookingRepository.sumParticipantsByEventId(event.getEventId());
    int remaining = event.getMaxParticipants() - alreadyBooked;

    // Prüfen ob genug Plätze
    if (cmd.numParticipants() > remaining) {
        throw new IllegalStateException("Nicht genügend Plätze verfügbar");
    }

    // Pending Booking im Cache speichern
    String pendingId = UUID.randomUUID().toString();
    pendingBookingCache.register(pendingId, ...);

    return new PendingBookingResponse(pendingId, bookingNumber, ...);
}
```

---

### PendingBookingCache

**Zweck**: Temporäre Speicherung von Buchungen vor Bestätigung

```java
@Component
public class PendingBookingCache {
    private final Map<String, PendingBooking> cache = new ConcurrentHashMap<>();

    public record PendingBooking(
        Long eventId,
        String bookingNumber,
        int numParticipants,
        BigDecimal priceTotal,
        String firstName,
        String lastName,
        String email,
        String phone,
        String bookingType,
        Instant expiresAt   // 15 Minuten nach Erstellung
    ) {}

    public void register(String pendingId, PendingBooking booking) {
        cache.put(pendingId, booking);
    }

    public Optional<PendingBooking> get(String pendingId) {
        PendingBooking pb = cache.get(pendingId);
        if (pb != null && pb.expiresAt().isAfter(Instant.now())) {
            return Optional.of(pb);
        }
        cache.remove(pendingId);  // Abgelaufen
        return Optional.empty();
    }
}
```

---

## 6. Domain-Modelle

### Übersicht aller Domain-Modelle

| Modell | Beschreibung | Wichtige Felder |
|--------|--------------|-----------------|
| `User` | Benutzer | id, email, password, role, vendorProfileId |
| `Event` | Veranstaltung | eventId, title, price, maxParticipants, status |
| `ActiveBooking` | Aktive Buchung | id, bookingNumber, eventId, numParticipants |
| `CancelledBooking` | Stornierte Buchung | wie ActiveBooking + cancelReason |
| `Participant` | Teilnehmer | id, firstName, lastName, checkedIn |
| `VendorProfile` | Vendor-Daten | id, userId, companyId, contactInfo |
| `Wishlist` | Wunschliste | wishlistId, userId, eventIds |

### Beziehungen zwischen Modellen

```
User (Benutzer)
 ├── 1:1 ──► VendorProfile (falls Vendor)
 ├── 1:n ──► Event (als Vendor erstellt)
 └── 1:1 ──► Wishlist

Event (Veranstaltung)
 ├── 1:n ──► ActiveBooking
 ├── 1:n ──► CancelledBooking
 └── 1:n ──► Participant

ActiveBooking / CancelledBooking
 └── 1:n ──► Participant
```

### Enums

**UserRole** - Benutzerrollen:
```java
public enum UserRole {
    CUSTOMER,     // Normaler Kunde
    VENDOR,       // Event-Anbieter
    BACKOFFICE,   // Backoffice-Mitarbeiter
    FRONTOFFICE,  // Check-In Personal
    ADMIN         // Administrator
}
```

**EventStatus** - Event-Zustände:
```java
public enum EventStatus {
    PLANNED,      // Geplant (noch nicht sichtbar)
    PUBLISHED,    // Veröffentlicht (buchbar)
    ACTIVE,       // Läuft gerade
    FULL,         // Ausgebucht
    CANCELLED     // Storniert
}
```

**Status** - Buchungsstatus:
```java
public enum Status {
    PENDING,           // Ausstehend
    CONFIRMED,         // Bestätigt
    PENDING_PAYMENT,   // Zahlung ausstehend
    PAID,              // Bezahlt
    PAYMENT_FAILED,    // Zahlung fehlgeschlagen
    REFUNDED,          // Erstattet
    CANCELLED          // Storniert
}
```

---

### Domain-Modell Beispiel: User

```java
public class User {
    private final Long id;
    private final String fname;
    private final String lname;
    private final String email;
    private final String password;
    private final UserRole role;
    private final Long vendorProfileId;
    private final Instant createdAt;
    private final Instant updatedAt;

    // Private Konstruktor - nur über Factory-Methoden
    private User(Long id, String fname, ...) { ... }

    // Factory-Methode für neue User
    public static User create(String fname, String lname, String email,
                              String password, UserRole role, Long vendorProfileId) {
        return new User(null, fname, lname, email, password, role, vendorProfileId,
                        Instant.now(), Instant.now());
    }

    // Factory-Methode für aus DB geladene User
    public static User restore(Long id, String fname, String lname, String email,
                               String password, UserRole role, Long vendorProfileId,
                               Instant createdAt, Instant updatedAt) {
        require(id != null, "id is required when restoring a user");
        return new User(id, fname, lname, email, password, role, vendorProfileId,
                        createdAt, updatedAt);
    }

    // Business-Methode: Beförderung
    public User promoteTo(UserRole newRole) {
        return new User(this.id, this.fname, this.lname, this.email, this.password,
                        newRole, this.vendorProfileId, this.createdAt, Instant.now());
    }

    // Immutabilität: Neue Instanz bei Änderung
    public User withUpdatedProfile(String fname, String lname) {
        return new User(this.id, fname, lname, this.email, this.password,
                        this.role, this.vendorProfileId, this.createdAt, Instant.now());
    }
}
```

**Wichtige Design-Prinzipien**:
1. **Immutabilität**: Alle Felder sind `final`
2. **Factory-Methoden**: `create()` für neue, `restore()` für aus DB
3. **Validierung**: Im Konstruktor, nicht außerhalb
4. **Keine Setter**: Änderungen nur über Methoden wie `withUpdatedProfile()`

---

## 7. Persistence Layer

### Die drei Komponenten

```
┌─────────────────────────────────────────────────────────────┐
│  Repository Port (Interface)                                 │
│  Location: /domain/repository/EventRepositoryPort.java       │
│  Definiert: Was die Domain braucht                           │
└─────────────────────────────────┬───────────────────────────┘
                                  │ implementiert
                                  ▼
┌─────────────────────────────────────────────────────────────┐
│  Repository Adapter (Implementation)                         │
│  Location: /infrastructure/jpa/adapter/EventRepositoryAdapter│
│  Macht: Übersetzt Port → JPA, verwendet Mapper               │
└─────────────────────────────────┬───────────────────────────┘
                                  │ verwendet
                                  ▼
┌─────────────────────────────────────────────────────────────┐
│  Spring Data JPA Repository                                  │
│  Location: /persistence/springdata/EventJpaRepository.java   │
│  Macht: Automatische SQL-Generierung                         │
└─────────────────────────────────────────────────────────────┘
```

### Repository Port (Interface)

```java
// /domain/repository/EventRepositoryPort.java
public interface EventRepositoryPort {
    Event save(Event event);
    Optional<Event> findById(Long id);
    boolean existsById(Long id);
    void deleteById(Long id);
    List<Event> findAll();
    List<Event> findByStatus(EventStatus status);
    List<Event> findByStatusIn(List<EventStatus> status);
}
```

**Wichtig**:
- Arbeitet mit **Domain-Objekten** (`Event`), nicht mit JPA-Entities
- Ist Framework-unabhängig
- Liegt im Domain-Package

### Repository Adapter

```java
// /infrastructure/jpa/adapter/EventRepositoryAdapter.java
@Component
public class EventRepositoryAdapter implements EventRepositoryPort {

    private final EventJpaRepository delegate;  // Spring Data Repo

    public EventRepositoryAdapter(EventJpaRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Event save(Event event) {
        // 1. Domain → JPA Entity
        EventJpaEntity entity = EventMapper.toEntity(event);

        // 2. Speichern
        EventJpaEntity saved = delegate.save(entity);

        // 3. JPA Entity → Domain
        return EventMapper.toDomain(saved);
    }

    @Override
    public Optional<Event> findById(Long id) {
        return delegate.findById(id)
            .map(EventMapper::toDomain);  // JPA Entity → Domain
    }
}
```

### Spring Data JPA Repository

```java
// /persistence/springdata/EventJpaRepository.java
@Repository
public interface EventJpaRepository extends JpaRepository<EventJpaEntity, Long> {

    // Spring Data generiert SQL automatisch aus Methodennamen
    List<EventJpaEntity> findByStatus(EventStatus status);

    List<EventJpaEntity> findByStatusIn(List<EventStatus> status);

    // Custom Query für komplexere Abfragen
    @Query("SELECT e FROM EventJpaEntity e WHERE e.vendorProfileId = :vendorId")
    List<EventJpaEntity> findByVendor(@Param("vendorId") Long vendorId);
}
```

### Mapper

```java
// /persistence/mapper/EventMapper.java
public class EventMapper {

    private EventMapper() {}  // Utility-Klasse, kein Konstruktor

    // JPA Entity → Domain Model
    public static Event toDomain(EventJpaEntity entity) {
        if (entity == null) return null;

        Event event = Event.createDraft();
        event.applyDetails(
            entity.getTitle(),
            entity.getCategory(),
            entity.getPrice(),
            entity.getMinParticipants(),
            entity.getMaxParticipants(),
            entity.getLocation(),
            // ... alle Felder
        );
        event.loadIdentifiers(entity.getEventId(), entity.getVendorProfileId());
        event.setStatus(entity.getStatus());
        return event;
    }

    // Domain Model → JPA Entity
    public static EventJpaEntity toEntity(Event domain) {
        if (domain == null) return null;

        EventJpaEntity entity = new EventJpaEntity();
        entity.setEventId(domain.getEventId());
        entity.setTitle(domain.getTitle());
        entity.setCategory(domain.getCategory());
        entity.setPrice(domain.getPrice());
        // ... alle Felder
        return entity;
    }
}
```

### JPA Entity

```java
// /persistence/model/EventJpaEntity.java
@Entity
@Table(name = "event")
public class EventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "title")
    private String title;

    @Column(name = "category")
    private String category;

    @Column(name = "price")
    private double price;

    @Column(name = "max_participants")
    private int maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus status;

    // Getters und Setters...
}
```

---

## 8. Request-Flow Beispiele

### Beispiel 1: Event erstellen

```
HTTP POST /api/events
Body: { "title": "Yoga Kurs", "price": 25.00, "maxParticipants": 20, ... }

┌────────────────────────────────────────────────────────────────────────┐
│ 1. EventRestController.createEventJson()                               │
│    - Empfängt HTTP Request                                             │
│    - Validiert Request Body (Jakarta Validation)                       │
│    - Wandelt CreateEventRequest → CreateEventCommand                   │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│ 2. EventService.createEvent(CreateEventCommand)                        │
│    - Validiert Business-Regeln (min < max Teilnehmer, Datum in Zukunft)│
│    - Erstellt Event-Domain-Objekt: Event.createDraft()                 │
│    - Setzt Details: event.applyDetails(...)                            │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│ 3. EventRepositoryPort.save(event)                                     │
│    (Interface-Aufruf, Spring injiziert EventRepositoryAdapter)         │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│ 4. EventRepositoryAdapter.save(event)                                  │
│    - EventMapper.toEntity(event) → EventJpaEntity                      │
│    - delegate.save(entity) → gespeicherte Entity                       │
│    - EventMapper.toDomain(saved) → Event mit ID                        │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│ 5. EventJpaRepository.save(entity)                                     │
│    - Hibernate generiert: INSERT INTO event (title, price, ...) VALUES │
│    - Datenbank gibt generierte ID zurück                               │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│ 6. Response                                                            │
│    - Event → EventResult → EventResponse (DTO)                         │
│    - HTTP 200 OK mit JSON                                              │
└────────────────────────────────────────────────────────────────────────┘
```

### Beispiel 2: Buchung erstellen und bestätigen

```
SCHRITT 1: Buchung erstellen (pending)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

HTTP POST /api/bookings
Body: { "eventId": 1, "firstName": "Max", "numParticipants": 2, ... }

1. BookingRestController empfängt Request
2. BookingService.createBooking():
   a) Event laden: eventRepository.findById(1)
   b) Kapazität prüfen: activeBookingRepository.sumParticipantsByEventId(1)
   c) Verfügbare Plätze berechnen: max - gebucht = remaining
   d) Prüfen: numParticipants ≤ remaining
   e) Buchungsnummer generieren: UUID
   f) PendingBooking im Cache speichern (15 Min TTL)
3. Response: { "pendingId": "abc-123", "bookingNumber": "EVT-XYZ", "priceTotal": 50.00 }

SCHRITT 2: Buchung bestätigen
━━━━━━━━━━━━━━━━━━━━━━━━━━━━

HTTP POST /api/bookings/confirm
Body: { "pendingId": "abc-123", "paymentMethod": "PAYPAL" }

1. BookingRestController empfängt Request
2. BookingService.confirmPendingBooking():
   a) Pending aus Cache laden: pendingBookingCache.get("abc-123")
   b) Event erneut laden (Kapazität kann sich geändert haben)
   c) Kapazität erneut prüfen
   d) ActiveBooking-Domain-Objekt erstellen
   e) Speichern: activeBookingRepository.save(booking)
   f) Pending aus Cache löschen
   g) Event.availableSlots aktualisieren
   h) Event speichern
3. Response: { "bookingId": 42, "bookingNumber": "EVT-XYZ", "status": "CONFIRMED" }
```

---

## 9. Billing Modul

### Übersicht

Das Billing-Modul ist ein **separater Bounded Context** für Rechnungsverwaltung.

```
/billing/
├── domain/
│   ├── model/          # Invoice, InvoiceLine, Money, Percentage
│   ├── service/        # InvoiceNumberGenerator
│   └── exception/      # InvoiceException, ImmutableInvoiceException
├── application/
│   ├── usecase/        # CreateInvoiceDraft, AddLine, Finalize, ...
│   └── dto/            # InvoiceDto, InvoiceLineDto
├── infrastructure/
│   ├── persistence/    # JPA Entities, Mapper, Repositories
│   ├── pdf/            # InvoicePdfGenerator (iText)
│   └── hash/           # InvoiceHashGenerator (SHA-256)
└── interfaces/
    └── rest/           # InvoiceController
```

### Rechnungs-Lebenszyklus

```
DRAFT (Entwurf)
    │
    ├── addLine() ──► Positionen hinzufügen
    ├── addShare() ──► Kostenverteilung
    │
    └── finalize() ──► FINAL (Unveränderbar)
                           │
                           ├── Invoice Number generiert
                           ├── PDF erstellt
                           └── Hash berechnet
```

### Use Cases

| Use Case | Beschreibung |
|----------|--------------|
| `CreateInvoiceDraftUseCase` | Neue Rechnung im Entwurf erstellen |
| `AddInvoiceLineUseCase` | Position hinzufügen (Beschreibung, Menge, Preis) |
| `AddInvoiceShareUseCase` | Kostenverteilung (Betrag oder Prozent) |
| `FinalizeInvoiceUseCase` | Rechnung abschließen, PDF generieren |
| `GetInvoiceUseCase` | Rechnung abrufen |

### REST Endpoints

| Methode | Endpoint | Beschreibung |
|---------|----------|--------------|
| `POST` | `/api/invoices` | Entwurf erstellen |
| `POST` | `/api/invoices/{id}/lines` | Position hinzufügen |
| `POST` | `/api/invoices/{id}/shares` | Verteilung hinzufügen |
| `POST` | `/api/invoices/{id}/finalize` | Abschließen |
| `GET` | `/api/invoices` | Alle Rechnungen |
| `GET` | `/api/invoices/{id}` | Einzelne Rechnung |
| `GET` | `/api/invoices/{id}/pdf` | PDF herunterladen |

### Domain-Modelle

**Invoice (Aggregate Root)**:
```java
Invoice
├── id: Long
├── eventId: Long
├── vendorId: Long
├── status: InvoiceStatus (DRAFT | FINAL | CANCELLED)
├── invoiceNumber: InvoiceNumber (Format: YYYY-NNNNN)
├── total: Money
├── hash: String (SHA-256)
├── pdfPath: String
├── lines: List<InvoiceLine>
└── shares: List<InvoiceShare>
```

**Value Objects**:
- `Money`: Geldbetrag mit Währung (EUR, 2 Dezimalstellen)
- `Percentage`: Prozentwert (0-100%)
- `InvoiceNumber`: Rechnungsnummer (2024-00001)

---

## 10. Sicherheit & Authentifizierung

### Spring Security Konfiguration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Öffentliche Endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/events").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()

                // Geschützte Endpoints
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/api/checkin/**").hasAnyAuthority("FRONTOFFICE", "BACKOFFICE")
                .requestMatchers("/api/bookings/**").authenticated()

                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Rollen-Berechtigungen

| Rolle | Berechtigungen |
|-------|----------------|
| `CUSTOMER` | Events anschauen, buchen, eigene Buchungen verwalten |
| `VENDOR` | Eigene Events erstellen/verwalten |
| `FRONTOFFICE` | Check-In durchführen |
| `BACKOFFICE` | Alle Events verwalten, Buchungen einsehen |
| `ADMIN` | Benutzer verwalten, Rollen ändern |

### Authentifizierungs-Flow

```
1. POST /api/auth/login { email, password }
2. AuthService.login():
   a) User aus DB laden
   b) Passwort mit BCrypt vergleichen
   c) Bei Erfolg: User zurückgeben
3. AuthController:
   a) Spring Security Context setzen
   b) Session erstellen
   c) User-DTO zurückgeben
4. Folgende Requests:
   - Session-Cookie wird mitgesendet
   - Spring Security prüft Session
   - Berechtigungen werden geprüft
```

---

## 11. Häufige Fragen (FAQ)

### Warum gibt es so viele Schichten?

**Antwort**: Die Schichten ermöglichen:
1. **Testbarkeit**: Jede Schicht kann isoliert getestet werden
2. **Wartbarkeit**: Änderungen in einer Schicht betreffen nicht die anderen
3. **Flexibilität**: Datenbank kann ausgetauscht werden ohne Business-Logik zu ändern

### Warum Domain-Modelle UND JPA-Entities?

**Antwort**: Trennung von Verantwortlichkeiten:
- **Domain-Modelle**: Enthalten Business-Logik, keine Framework-Abhängigkeiten
- **JPA-Entities**: Nur für Datenbank-Mapping, keine Business-Logik

Das ermöglicht:
- Domain-Modelle zu ändern ohne DB-Schema zu ändern
- DB-Schema zu ändern ohne Domain zu ändern
- Einfacheres Testing der Business-Logik

### Warum Repository Ports statt direkt JPA?

**Antwort**: Dependency Inversion Principle:
- Services kennen nur das Interface (`EventRepositoryPort`)
- Sie wissen nicht, dass dahinter JPA steht
- Man könnte JPA durch MongoDB ersetzen → nur Adapter ändern

### Was ist der Unterschied zwischen Service und Use Case?

**Antwort**:
- **Use Case Interface**: Definiert WAS getan werden kann (Vertrag)
- **Service**: Implementiert WIE es getan wird (Logik)

Das ermöglicht:
- Verschiedene Implementierungen (z.B. für Tests)
- Klare API-Definition
- Dokumentation der verfügbaren Operationen

### Wie funktioniert die Buchungs-Reservierung?

**Antwort**: Zweistufiger Prozess:
1. **Pending Booking**: Wird im In-Memory-Cache gespeichert (15 Min TTL)
   - Schnell, keine DB-Transaktion
   - Kapazität wird NICHT sofort reserviert
2. **Confirm Booking**: Wird in DB gespeichert
   - Kapazität wird erneut geprüft
   - Erst jetzt wird Platz verbindlich reserviert

### Warum kein ORM direkt in Domain-Modellen?

**Antwort**: Domain-Driven Design Prinzip:
- Domain-Modelle sollen "pure" sein
- Keine `@Entity`, `@Column` etc.
- Ermöglicht Framework-unabhängige Business-Logik
- Einfacheres Unit-Testing

### Wie werden Transaktionen verwaltet?

**Antwort**: Mit `@Transactional` auf Service-Methoden:
```java
@Service
public class BookingService {

    @Transactional  // Alle DB-Operationen in einer Transaktion
    public ActiveBooking confirmPendingBooking(ConfirmBookingCommand cmd) {
        // Mehrere Saves → alle oder keine
        activeBookingRepository.save(booking);
        eventRepository.save(event);
        // Bei Exception: Rollback beider Operationen
    }

    @Transactional(readOnly = true)  // Optimierung für Lese-Operationen
    public ActiveBooking getBooking(Long id) {
        return activeBookingRepository.findById(id).orElseThrow(...);
    }
}
```

---

## Zusammenfassung

### Die wichtigsten Konzepte

1. **Hexagonale Architektur**: Klare Trennung zwischen Core (Domain) und Infrastructure
2. **Ports & Adapters**: Interfaces für Ein- und Ausgänge
3. **Domain-Driven Design**: Reichhaltige Domain-Modelle mit Business-Logik
4. **Mapper-Pattern**: Trennung von Domain und Persistence
5. **Dependency Injection**: Spring verwaltet alle Abhängigkeiten

### Request-Flow Kurzfassung

```
HTTP Request
    ↓
REST Controller (validiert, konvertiert)
    ↓
Use Case Interface (Vertrag)
    ↓
Service Implementation (Business-Logik)
    ↓
Repository Port (Interface)
    ↓
Repository Adapter (Implementierung)
    ↓
Mapper (Domain ↔ Entity)
    ↓
Spring Data JPA Repository
    ↓
Datenbank
```

### Schichten-Verantwortlichkeiten

| Schicht | Macht | Macht NICHT |
|---------|-------|-------------|
| Controller | Validierung, Konvertierung, HTTP | Business-Logik |
| Service | Orchestrierung, Business-Logik | HTTP, SQL |
| Domain | Geschäftsregeln, Validierung | Framework-Code |
| Repository Port | Vertrag definieren | Implementieren |
| Adapter | Port → JPA übersetzen | Business-Logik |
| JPA Repository | SQL generieren | Wissen über Domain |
