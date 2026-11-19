# CLAUDE.md – LocalTix

Utilizatorul va scrie de obicei: **„Sunt [nume], membrul [A/B/C]. Hai să facem task-ul T-x.y.”**
Găsește task-ul în backlog (secțiunea 6), lucrează **doar** la el și respectă regulile TDD (secțiunea 5).

### ⚠️ PASUL 0 – OBLIGATORIU, înainte de ORICE task (inclusiv RED)
Nu scrie niciun rând de cod până nu ai făcut acești pași:
1. **Citește codul existent din proiect**: `model`, `repository`, `service`, `controller`, `templates` (inclusiv `fragments/layout.html`), `static/css/style.css`, `data.sql`, `application.properties` și testele din `src/test`. Rulează `git log --oneline` ca să vezi ce task-uri au fost deja făcute.
2. **Citește notele „⚠️ Atenție” de la story-ul tău din backlog** (secțiunea 6) – acolo sunt lucruri lăsate de colegi de care depinde implementarea ta.
3. **Spune pe scurt utilizatorului** ce ai găsit relevant pentru task (ce există deja, ce poți refolosi, ce lipsește).
4. **Corelează-te cu codul existent**: folosește aceleași entități, enum-uri, servicii, repository-uri, layout comun și același `data.sql`. **Nu crea duplicate sau variante paralele** (ex. a doua clasă `User`, alt layout, alt fișier CSS). Dacă trebuie să extinzi o clasă existentă, extinde-o fără să strici ce folosesc colegii; testele existente trebuie să rămână verzi.
5. Dacă un task de care depinde cel curent lipsește, spune-o și fă doar minimul necesar, apoi **adaugă o notă „⚠️ Atenție” în backlog** la story-ul colegului care va trebui să continue sau să înlocuiască acel minim.

---

## 1. Aplicația

**LocalTix** – platformă web de bilete pentru evenimente locale (concerte, stand-up, teatru independent).

Clientul își face cont, caută și filtrează evenimente, vede prețul cu reducerile aplicabile, își alege locul pe harta sălii, pune biletele în coș, plătește online (simulat), primește bilet electronic cu cod unic în cont și poate cere rambursare.

Toate textele din interfață (pagini, butoane, etichete, mesaje de eroare și de confirmare) sunt în **limba engleză**.

---

## 2. Echipa

Toți trei membrii sunt developeri. **Fiecare task are un singur responsabil** – nu împărți un task între membri.

| Membru | Nume | Rol |
|---|---|---|
| **A** | David | Product Owner + developer |
| **B** | Maria | Scrum Master + developer |
| **C** | Ana | Developer |

---

## 3. Stack și reguli de dezvoltare

### Stack
- **Java 21**, **Maven** prin wrapper (`./mvnw`)
- **Spring Boot 4**: Spring Web MVC, Thymeleaf, Spring Data JPA, H2 (în memorie, consolă la `/h2-console`), Validation
- **JUnit (Jupiter)** prin `spring-boot-starter-test` – nu adăuga altă versiune de JUnit
- **Bootstrap** din CDN pentru stil; JavaScript doar unde e strict necesar (ex. cronometrul din coș)
- groupId `ro.ase`, artifactId `localtix`, pachet de bază `ro.ase.localtix`

**Atenție:** Spring Boot 4 are starter-e cu denumiri noi (ex. `spring-boot-starter-webmvc`, `spring-boot-h2console`). Nu le înlocui cu denumirile din Spring Boot 3 și nu schimba versiunea de Spring Boot.

### Structura pachetelor
```
src/main/java/ro/ase/localtix
├── model        → entități JPA și enum-uri (User, Event, Seat, Ticket, Order, Category...)
├── repository   → interfețe Spring Data JPA
├── service      → logica de business (aici se face TDD)
└── controller   → controllere MVC
src/main/resources
├── templates    → pagini Thymeleaf
├── static       → css/js proprii (minim)
├── application.properties
└── data.sql     → date de test (evenimente, săli, locuri)
src/test/java/ro/ase/localtix/service → teste unitare, câte o clasă de test per serviciu
```

### Reguli de cod
1. **Logica de business stă în `service`, în Java simplu**, fără Spring și fără bază de date în clasele testate, ca testele unitare să ruleze fără context Spring.
2. **Nu folosi `LocalDateTime.now()` în logica de business.** Ora curentă se primește ca parametru (`LocalDateTime now`), ca regulile cu 10 minute / 15 minute / 48 de ore / 14 zile să fie testabile.
3. **Tot codul este în engleză**: nume de clase, metode, metode de test, variabile, parametri, câmpuri, constante, enum-uri, excepții, comentarii, nume de template-uri și URL-uri (ex. `PriceCalculator.calculate(basePrice, category, daysBefore)`, `studentGets30PercentDiscount()`). **Toate mesajele afișate utilizatorului sunt tot în engleză.** Doar explicațiile date în chat sunt în română.
4. Sumele de bani se rotunjesc la 2 zecimale.
5. Erorile de business se semnalează prin excepții cu mesaje clare în engleză (afișate în interfață) (ex. `IllegalArgumentException` sau excepții proprii ca `SeatUnavailableException`).
6. Cod simplu și lizibil – membrii echipei trebuie să-l poată explica. Fără over-engineering.
7. După fiecare fază, explică pe scurt și simplu codul scris.
8. Nu face commit/push decât dacă ți se cere. Format mesaj: `T-6.2 [GREEN] implementare PriceCalculator`.

---

## 4. Aspect și temă vizuală

Toată aplicația are **aceeași temă**: **dark mode**, modernă, cu accente de **albastru și mov** și text alb. Fiecare pagină nouă trebuie să arate ca făcând parte din aceeași aplicație.

### Paletă de culori
| Rol | Culoare |
|---|---|
| Fundal pagină | `#0f0f1a` (aproape negru, cu tentă albastră) |
| Fundal carduri / formulare / navbar | `#1a1a2e` |
| Borduri / separatoare | `#2e2e4a` |
| Accent principal (butoane, linkuri) | albastru `#4f6bff` |
| Accent secundar (hover, badge-uri, evidențieri) | mov `#8b5cf6` |
| Gradient pentru titluri și butoane principale | `linear-gradient(135deg, #4f6bff, #8b5cf6)` |
| Text principal | alb `#ffffff` |
| Text secundar | gri deschis `#a0a0c0` |
| Succes / eroare / avertizare | `#22c55e` / `#ef4444` / `#f59e0b` |

### Reguli
1. Bootstrap rulează în **dark mode** (`<html data-bs-theme="dark">`); culorile proprii se pun într-un singur fișier, `static/css/style.css`, ca **variabile CSS** (`:root { --lt-bg: ...; }`), reutilizate peste tot.
2. Toate paginile folosesc **același layout comun** (fragment Thymeleaf: navbar cu logo „LocalTix”, conținut, footer). Nu se duplică navbar-ul în fiecare pagină.
3. Carduri cu colțuri rotunjite, umbre discrete și un efect subtil de hover (glow albastru/mov); spațiere generoasă.
4. Butoanele principale folosesc gradientul albastru → mov; cele secundare sunt outline.
5. Mesajele de eroare și de confirmare folosesc alertele Bootstrap în culorile de mai sus.
6. Harta sălii (US-7): locuri **libere** albastru, **blocate temporar** mov/estompat, **vândute** gri închis, **selectate** alb cu contur albastru.
7. Aplicația trebuie să arate bine și pe mobil (grila Bootstrap, responsive).

---

## 5. Reguli TDD – OBLIGATORII

### 📸 Fiecare user story are OBLIGATORIU 3 capturi: RED, GREEN, REFACTOR
- **Fiecare user story** trece prin **toate cele trei faze TDD**, în ordinea **[RED] → [GREEN] → [REFACTOR]**, și pentru fiecare fază utilizatorul face o **captură de ecran** (testele roșii; testele verzi; codul refactorizat cu testele verzi). Task-ul [UI] vine după aceste trei faze.
- Dacă un story din backlog nu are una dintre faze, **adaug-o** în backlog înainte de a începe și spune-i utilizatorului.
- La sfârșitul fiecărei faze amintește-i utilizatorului ce captură trebuie să facă.

După fiecare fază, utilizatorul face o **captură de ecran**. De aceea, **după fiecare fază te OPREȘTI** și aștepți confirmarea. Nu trece niciodată singur la faza următoare.

### [RED]
1. Scrie **doar testele** pentru criteriile de acceptare ale story-ului, în `src/test/java/ro/ase/localtix/service/<Clasa>Test.java`.
2. Creează **doar scheletul minim** al clasei/metodelor testate (returnează `0`, `null`, `false`), astfel încât codul **să compileze** și testele să pice **pe aserțiuni**, nu pe erori de compilare.
3. **Nu implementa logica.**
4. Spune ce clasă de test trebuie rulată și ce rezultat roșu e de așteptat. Pentru fiecare test, spune ce criteriu de acceptare verifică.
5. **STOP:** „📸 Rulează testele și fă captura cu testele ROȘII. Spune-mi când trecem la GREEN.”

### [GREEN]
1. Scrie **codul minim** care face toate testele să treacă.
2. **Nu modifica testele** ca să treacă. Dacă un test pare greșit, spune și întreabă.
3. **STOP:** „📸 Rulează testele și fă captura cu testele VERZI. Spune-mi când trecem la REFACTOR.”

### [REFACTOR]
1. Îmbunătățește codul **fără să-i schimbi comportamentul**: constante în loc de numere „magice”, enum-uri, metode extrase, excepții proprii, nume mai clare.
2. Nu adăuga funcționalitate nouă. Testele trebuie să rămână verzi.
3. Testele **nu se modifică** la REFACTOR (dacă o îmbunătățire ar cere schimbarea testelor, nu o face sau întreabă).
4. Spune pe scurt ce s-a îmbunătățit față de GREEN (înainte / după).
5. **STOP:** „📸 Rulează testele și fă captura cu codul refactorizat și testele VERZI.”

### [UI] și alte task-uri fără teste
Construiește pagina Thymeleaf și controllerul, folosind serviciile deja implementate. La final spune la ce URL se vede pagina (`http://localhost:8080/...`) și explică utilizatorului cum să verifice pagina în browser (vezi mai jos). **STOP** la finalul task-ului.

### Ghid de verificare a UI-ului în browser (obligatoriu la final de task [UI])
După orice task [UI], dă-i utilizatorului un ghid scurt, pas cu pas, ca să poată verifica singur pagina:
1. **Pornirea aplicației**: în IntelliJ, ▶️ Run pe `LocaltixApplication` (sau `./mvnw spring-boot:run` în terminal) și așteaptă mesajul `Started LocaltixApplication`. Dacă apare „Port 8080 was already in use”, aplicația rulează deja și trebuie oprită acolo.
2. **URL-ul exact** al paginii și, dacă e cazul, cu ce utilizator să se autentifice (date din `data.sql`).
3. **Scenarii de verificat, câte unul pentru fiecare criteriu de acceptare**, cu **valori concrete** de introdus (ex. telefon `0812345678`) și **ce trebuie să apară exact** (mesajul în engleză, culoarea alertei, ce se schimbă pe pagină). Include atât un caz valid, cât și cazuri invalide.
4. **Verificarea în baza de date (opțional)**: consola H2 la `http://localhost:8080/h2-console`, cu JDBC URL `jdbc:h2:mem:localtix`, user `sa`, fără parolă, plus un `SELECT` util (ex. `SELECT * FROM users;`).
5. **Precizează că datele se resetează** la fiecare repornire (H2 în memorie + `data.sql`), deci modificările făcute în test dispar după restart.
6. **Ce să captureze** (📸) pentru documentație: pagina în starea inițială, un caz de eroare și un caz de succes.
7. **Oprirea aplicației**: ⏹ Stop în IntelliJ sau Ctrl+C în terminal.

Dacă poți porni tu aplicația, verifică scenariile (ex. cu `curl`) înainte să-i dai ghidul și oprește apoi aplicația, ca portul 8080 să rămână liber.

Dacă task-ul cerut depinde de ceva ce nu există încă (ex. o entitate), spune-o și fă doar minimul necesar.

---

## 6. Backlog

> ⚠️ **Atenție – pentru toate task-urile [UI] (lăsat de Ana, US-3):**
> - Există deja **layoutul comun** `templates/fragments/layout.html` (fragmentele `head('Titlu')`, `navbar`, `footer`) și **tema** `static/css/style.css` (variabile `--lt-*`, clase `lt-card`, `btn-lt-primary`, `btn-lt-outline`, `lt-gradient-text`, `lt-muted`). Orice pagină nouă le folosește – vezi `templates/profile.html` ca exemplu. Link-urile noi se adaugă în navbar-ul comun.
> - Datele de test se adaugă în `data.sql` existent (nu se creează alt fișier). Tabelele se creează din entități (`ddl-auto=create-drop`), iar `data.sql` rulează după aceea.

### SPRINT 1 – Conturi

#### US-1 Creare cont — responsabil **A** ✅ implementat (T-1.1 – T-1.4)
Ca vizitator, vreau să îmi creez un cont, pentru a putea cumpăra bilete.
Criterii de acceptare:
1. Câmpuri obligatorii: nume, prenume, email, telefon, data nașterii, parolă, confirmare parolă.
2. Emailul are format valid și nu există deja în sistem.
3. Telefonul are format românesc (07xxxxxxxx).
4. Utilizatorul are minimum 14 ani.
5. Parola: minimum 8 caractere, cel puțin o literă mare, o cifră și un simbol.
6. Parola și confirmarea coincid.
7. La date invalide se afișează un mesaj de eroare clar pentru fiecare câmp.

- **T-1.1 [RED]** – teste `RegistrationValidatorTest`
- **T-1.2 [GREEN]** – implementare `RegistrationValidator`
- **T-1.3 [REFACTOR]** – regex-uri și limite în constante, mesaje de eroare centralizate
- **T-1.4 [UI]** – formular de înregistrare + controller + salvare User

> ⚠️ **Atenție (lăsat de Ana, US-3):**
> - Entitatea `model/User.java` **există deja** (tabela `users`, câmpuri `lastName`, `firstName`, `email`, `phone`, `birthDate`). **Complet-o** cu ce mai trebuie (ex. `password`), nu crea altă clasă și nu redenumi câmpurile existente – le folosesc `ProfileService` și pagina de profil.
> - `repository/UserRepository.java` există deja și are `findAllEmails()` – folosește-l pentru verificarea că emailul nu există deja.
> - Formatul telefonului e același ca în profil: `07\d{8}`, cu mesajul „Phone number must have the format 07xxxxxxxx”.
> - Dacă adaugi coloane obligatorii (ex. `password`), actualizează și utilizatorii demo din `data.sql`, altfel aplicația nu mai pornește.
> - Pagina de înregistrare folosește layoutul comun (`fragments/layout.html`) și `style.css`.

> ⚠️ **Atenție (lăsat de Maria, US-2):**
> - `User` are deja câmpurile `password`, `failedAttempts` (implicit 0) și `lockedUntil`, adăugate pentru login. Nu le redenumi – le folosește `AuthService`. La înregistrare setează doar `password` (cu `setPassword`).
> - Utilizatorii demo din `data.sql` au deja parola `Parola123!`. Parola se salvează în clar (comparată cu `equals` în `AuthService`); dacă introduci hashing, actualizează și `AuthService.isPasswordCorrect` și `data.sql`.
> - Login-ul există: `/login`, `/logout` (`LoginController`). Utilizatorul logat e în sesiune cu cheia `ProfileController.SESSION_USER_ID`. După înregistrare poți redirecționa spre `/login`.
> - `UserRepository.findByEmailIgnoreCase(email)` există (folosit la login).

#### US-2 Autentificare — responsabil **B** ✅ implementat (T-2.1 – T-2.4)
Ca utilizator înregistrat, vreau să mă autentific cu email și parolă, pentru a avea acces la contul meu.
Criterii de acceptare:
1. Cu email și parolă corecte, utilizatorul intră în cont.
2. La parolă greșită sau cont inexistent apare „Invalid email or password”, fără să precizeze care e greșit.
3. După 3 încercări greșite consecutive, contul se blochează 15 minute.
4. O autentificare reușită resetează numărul de încercări greșite.
5. Utilizatorul se poate deconecta oricând.

- **T-2.1 [RED]** – teste `AuthServiceTest`
- **T-2.2 [GREEN]** – implementare `AuthService`
- **T-2.3 [REFACTOR]** – constante (3 încercări, 15 minute), curățare cod
- **T-2.4 [UI]** – pagina de login + logout (sesiune HTTP simplă)

> ✅ Notele de mai jos au fost rezolvate la T-2.4 (fallback-ul demo a fost șters, link-urile Login/Logout sunt în navbar, utilizatorii demo au parolă).
>
> ⚠️ **Atenție (lăsat de Ana, US-3):**
> - Utilizatorul logat se ține în sesiune cu cheia `ProfileController.SESSION_USER_ID` (`"userId"`). La login: `session.setAttribute(ProfileController.SESSION_USER_ID, user.getId())`; la logout: `session.invalidate()`.
> - Până la login, `ProfileController` folosește **temporar** utilizatorul demo cu id 1 (`DEMO_USER_ID`). După ce login-ul funcționează, **șterge acest fallback** din `getCurrentUser(...)` și redirecționează spre pagina de login dacă nu e nimeni logat.
> - Adaugă link-urile „Login” / „Logout” în navbar-ul din `fragments/layout.html` (nu face alt navbar). Pagina de login folosește layoutul comun și `style.css`.
> - Utilizatorii demo din `data.sql` vor avea nevoie de parolă (vezi US-1).

#### US-3 Pagina de profil — responsabil **C** ✅ implementat (T-3.1 – T-3.4)
Ca utilizator autentificat, vreau să îmi văd și să îmi modific datele personale.
Criterii de acceptare:
1. Se afișează nume, email, telefon, data nașterii.
2. Se pot modifica numele, telefonul și emailul.
3. Emailul nou nu poate fi folosit deja de alt cont.
4. Telefonul nou respectă formatul românesc.
5. După salvare apare un mesaj de confirmare.

- **T-3.1 [RED]** – teste `ProfileServiceTest`
- **T-3.2 [GREEN]** – implementare `ProfileService`
- **T-3.3 [UI]** – pagina de profil (vizualizare + editare)
- **T-3.4 [REFACTOR]** – regex și mesaje în constante, metode extrase `validateEmail` / `validatePhone` (făcut după UI)

### SPRINT 2 – Catalog evenimente

#### US-4 Listă de evenimente — responsabil **C** ✅ implementat (T-4.1 – T-4.5)
Ca vizitator, vreau să văd lista evenimentelor disponibile, pentru a alege la ce eveniment să merg.
Criterii de acceptare:
1. Pentru fiecare eveniment: titlu, categorie (concert / stand-up / teatru), oraș, locație, dată și oră, preț de pornire, locuri disponibile.
2. Sortare după dată, cele mai apropiate primele.
3. Evenimentele trecute nu apar.
4. Click pe un eveniment deschide pagina de detalii cu descrierea completă.

- **T-4.1 [SETUP]** – entități Event și Seat + repository-uri + evenimente de test în `data.sql`
- **T-4.2 [RED]** – teste `EventCatalogServiceTest` (doar evenimentele viitoare, sortate după dată, număr de locuri disponibile; ora curentă primită ca parametru `now`)
- **T-4.3 [GREEN]** – implementare `EventCatalogService`
- **T-4.4 [REFACTOR]** – metode extrase, nume mai clare, curățare cod
- **T-4.5 [UI]** – pagina catalog + pagina de detalii eveniment

> ℹ️ **Cum verifici datele din baza de date (H2 Console) — pas cu pas:**
> 1. Pornește aplicația (▶️ în IntelliJ sau `./mvnw spring-boot:run`)
> 2. Deschide `http://localhost:8080/h2-console` în browser
> 3. Setează: **JDBC URL** = `jdbc:h2:mem:localtix` · **User Name** = `sa` · **Password** = *(gol)* → click **Connect**
> 4. Query-uri utile:
>    ```sql
>    -- Toți utilizatorii (email + parolă demo: Parola123!)
>    SELECT id, first_name, last_name, email FROM users;
>    -- Toate evenimentele, sortate după dată
>    SELECT id, title, category, city, date_time, base_price FROM events ORDER BY date_time;
>    -- Locuri disponibile per eveniment
>    SELECT event_id, status, COUNT(*) AS count FROM seats GROUP BY event_id, status ORDER BY event_id;
>    ```
> 5. ⚠️ Datele se resetează la fiecare repornire (H2 în memorie + `data.sql`).

> ⚠️ **Atenție (lăsat de Ana, US-4):**
> - **Entitățile `Event` și `Seat` există deja** în `model/`. Nu le recrea. Câmpurile `Event`: `id`, `title`, `category` (enum `EventCategory`), `city`, `location`, `dateTime`, `basePrice` (BigDecimal), `description`. Câmpurile `Seat`: `id`, `event` (ManyToOne), `rowLabel`, `seatNumber`, `status` (enum `SeatStatus`).
> - **`EventCategory`** (CONCERT / STANDUP / THEATRE) este tipul evenimentului — are și `getDisplayName()` și `getBadgeClass()` pentru UI. **Nu e același lucru** cu categoria de preț (NORMAL / STUDENT / SENIOR) pe care o creează David la US-6.
> - **`SeatStatus`** (AVAILABLE / LOCKED / SOLD) există deja în `model/SeatStatus.java`.
> - **`EventRepository`** și **`SeatRepository`** (cu `findByEventId(Long)`) există în `repository/`.
> - **`EventCatalogService`** există cu metodele: `getUpcomingEvents(LocalDateTime now)` (filtrează trecute + sortează), `countAvailableSeats(Long eventId)`, `findById(Long id)`.
> - **Pagina catalog** există la `/events` (`templates/events.html`) și **pagina de detalii** la `/events/{id}` (`templates/event-detail.html`). Linkul „Events" e deja în navbar.
> - **`data.sql`** are 5 evenimente (4 viitoare, 1 trecut) și 45 de locuri. Poți adăuga sau modifica date de test acolo.
> - Clasa de test este `EventCatalogServiceTest` (10 teste, toate verzi).

#### US-5 Filtrare evenimente — responsabil **B** ✅ implementat (T-5.1 – T-5.4)
Ca vizitator, vreau să filtrez evenimentele după categorie, oraș, dată și preț.
Criterii de acceptare:
1. Filtrele se pot combina (ex. stand-up în București sub 100 lei).
2. Filtrul de dată acceptă un interval.
3. Filtrul de preț acceptă minim și maxim.
4. Fără rezultate se afișează „No events found”.
5. Filtrele se pot reseta dintr-un buton.

- **T-5.1 [RED]** – teste `EventFilterServiceTest`
- **T-5.2 [GREEN]** – implementare `EventFilterService`
- **T-5.3 [REFACTOR]** – fiecare filtru într-o metodă separată, constante, curățare cod
- **T-5.4 [UI]** – formular de filtre pe pagina catalog

> ⚠️ **Atenție (lăsat de Ana, US-4) — pentru US-5 [UI]:**
> - Formularul de filtre se adaugă pe pagina catalog existentă (`templates/events.html`), nu pe o pagină separată. Pagina primește deja `events` (lista) și `seatCounts` (Map) din `EventCatalogController`.
> - `EventCatalogController` va trebui extins (sau înlocuit) ca să accepte și parametrii de filtrare și să apeleze `EventFilterService`.

> ⚠️ **Atenție (lăsat de David, US-6) — pentru US-5:**
> - `EventCatalogController` conține acum și logica de afișare a prețurilor pe `/events/{id}` (metoda `addPrices`, record-ul `TicketPrice`, câmpul `priceCalculator`). Când extinzi controllerul pentru filtre, **nu șterge** aceste părți – modifică doar `showCatalog` (`/events`).
> - Filtrul de preț (CA3) se aplică pe `Event.basePrice` (prețul de bază, fără reduceri) – așa apare și „Starting price” în catalog.

> ⚠️ **Atenție (lăsat de Maria, US-5) — ce există acum, pentru toată echipa:**
> - **`service/EventFilter`** (record): `category`, `city`, `dateFrom`, `dateTo` (LocalDate), `minPrice`, `maxPrice` (BigDecimal). `null` / gol = filtrul nu se aplică. În Thymeleaf se accesează cu paranteze: `${filter.city()}`.
> - **`service/EventFilterService.filter(List<Event> events, EventFilter filter)`** – filtrează lista primită (de la `getUpcomingEvents(now)`), păstrând ordinea. Intervalele inversate aruncă `IllegalArgumentException` cu mesajele din constantele `INVALID_DATE_INTERVAL` / `INVALID_PRICE_INTERVAL`.
> - **`EventCatalogController`** are acum în constructor și `EventFilterService`; `showCatalog` (`/events`) primește parametrii opționali `category`, `city`, `dateFrom`, `dateTo`, `minPrice`, `maxPrice` și pune în model `filter`, `filtersApplied`, `categories`, `cities`, `errorMessage` (în plus față de `events` și `seatCounts`). `showEventDetail` / `addPrices` nu au fost atinse.
> - CSS nou în `style.css`: `form-select` în tema dark, `lt-filter-card` (card fără glow la hover).
> - `data.sql` are acum un al 6-lea eveniment (`Open Mic Comedy`, STANDUP, București, 60 RON, 5 locuri) – dacă adaugi evenimente noi, ele primesc id-ul 7+.
> - Testele: `EventFilterServiceTest` (14 teste, toate verzi).

#### US-6 Calcul preț bilet — responsabil **A** ✅ implementat (T-6.1 – T-6.4)
Ca client, vreau să văd prețul corect al biletului în funcție de categoria mea și de momentul cumpărării.
Criterii de acceptare:
1. Categorii: normal (preț întreg), student (−30%), pensionar (−40%).
2. Early bird: −20% dacă biletul e cumpărat cu mai mult de 14 zile înainte de eveniment.
3. Reducerile nu se cumulează – se aplică doar cea mai mare.
4. Prețul final se rotunjește la 2 zecimale; un preț de bază negativ aruncă excepție.
5. Se afișează prețul inițial, reducerea aplicată și prețul final.

- **T-6.1 [RED]** – teste `PriceCalculatorTest`
- **T-6.2 [GREEN]** – implementare `PriceCalculator.calculate(basePrice, category, daysBefore)`
- **T-6.3 [REFACTOR]** – procentele mutate în enum-ul `Category`, constante pentru early bird
- **T-6.4 [UI]** – afișare preț inițial / reducere / preț final pe pagina evenimentului

> ✅ Nota de mai jos a fost rezolvată la US-6 (enum-ul se numește `TicketCategory`, prețurile sunt pe `/events/{id}`).
>
> ⚠️ **Atenție (lăsat de Ana, US-4):**
> - **`EventCategory`** (CONCERT / STANDUP / THEATRE) este tipul evenimentului și există deja — **nu** e enum-ul de prețuri. Enum-ul de prețuri pe care îl creezi tu (NORMAL / STUDENT / SENIOR) trebuie să aibă un alt nume (ex. `TicketCategory`) ca să nu intre în conflict.
> - **`Event.basePrice`** (BigDecimal) există deja — acesta e parametrul `basePrice` din `PriceCalculator.calculate(...)`.
> - Pagina de detalii eveniment există la `/events/{id}` (`templates/event-detail.html`) — la T-6.4 [UI] adaugi prețul calculat pe această pagină, nu creezi alta.

> ⚠️ **Atenție (lăsat de David, US-6) — ce există acum, pentru toată echipa:**
> - **`model/TicketCategory`** = categoria de preț a clientului: `NORMAL("Normal", 0)`, `STUDENT("Student", 30)`, `SENIOR("Senior", 40)`, cu `getDisplayName()` și `getDiscountPercent()`. **Nu e același lucru cu `EventCategory`** (tipul evenimentului). Procentele se schimbă doar aici.
> - **`service/PriceCalculator`** (Java simplu, fără Spring – se creează cu `new PriceCalculator()`):
>   - `calculate(BigDecimal basePrice, TicketCategory category, long daysBefore)` → prețul final (BigDecimal, 2 zecimale, `HALF_UP`); preț de bază negativ → `IllegalArgumentException("Base price cannot be negative")`.
>   - `getDiscountPercent(category, daysBefore)` → reducerea aplicată în procente (doar cea mai mare – reducerile **nu** se cumulează).
>   - Constante publice: `EARLY_BIRD_MIN_DAYS = 14` (early bird doar la **mai mult** de 14 zile), `EARLY_BIRD_DISCOUNT_PERCENT = 20`, `NEGATIVE_BASE_PRICE`.
> - **`daysBefore`** se calculează în controller: `ChronoUnit.DAYS.between(LocalDateTime.now(), event.getDateTime())` (zile întregi). În servicii nu folosiți `now()` – primiți `now` sau `daysBefore` ca parametru.
> - **Pagina `/events/{id}`** are acum cardul „Ticket prices” (tabel: categorie / preț inițial / reducere / preț final + alertă „Early bird offer!”). Datele vin din `EventCatalogController.showEventDetail` → metoda privată `addPrices(...)`, care pune în model `prices` (listă de `EventCatalogController.TicketPrice` – record; în Thymeleaf se accesează cu paranteze, ex. `${price.finalPrice()}`), `earlyBird`, `earlyBirdPercent`, `earlyBirdDays`. **Dacă modificați controllerul, păstrați apelul `addPrices(event, model)`**, altfel pagina de detalii nu mai pornește.
> - CSS nou în `style.css`: `lt-table` (tabel pe fundal dark), `lt-badge-discount` (badge mov), `lt-final-price`.
> - Testele: `PriceCalculatorTest` (15 teste, toate verzi).

### SPRINT 3 – Cumpărare bilete

#### US-7 Alegere loc în sală — responsabil **C** ✅ implementat (T-7.1 – T-7.4)
Ca client, vreau să îmi aleg locul pe harta sălii.
Criterii de acceptare:
1. Harta arată locurile libere, ocupate și blocate temporar, în culori diferite.
2. Un loc deja vândut nu mai poate fi selectat.
3. Un loc pus în coș se blochează 10 minute pentru ceilalți clienți.
4. Dacă plata nu se face în 10 minute, locul se eliberează automat.
5. Dacă doi clienți aleg același loc, îl primește doar primul; al doilea vede o eroare.

- **T-7.1 [RED]** – teste `SeatReservationServiceTest`
- **T-7.2 [GREEN]** – implementare `SeatReservationService`
- **T-7.3 [REFACTOR]** – constantă 10 minute, excepție proprie *(enum-ul `SeatStatus` există deja, vezi nota de mai jos)*
- **T-7.4 [UI]** – harta sălii (grilă de locuri colorate) + selectare

> ⚠️ **Atenție (lăsat de Ana, US-4):**
> - **`SeatStatus`** (AVAILABLE / LOCKED / SOLD) **există deja** în `model/SeatStatus.java` — nu-l recrea.
> - **`Seat`** există deja în `model/Seat.java` cu câmpurile: `id`, `event` (ManyToOne), `rowLabel`, `seatNumber`, `status` (SeatStatus). Extinde sau modifică ce e necesar fără să strici `countAvailableSeats` din `EventCatalogService`.
> - **`SeatRepository`** există cu `findByEventId(Long eventId)` — poți adăuga metode noi fără să le ștergi pe cele existente.
> - Pagina de detalii eveniment (`/events/{id}`) are deja un buton „Get Tickets" (dezactivat când sold-out). La T-7.4 [UI] conectezi acest buton la harta sălii.

> ⚠️ **Atenție (lăsat de Ana, US-7) — ce există acum, pentru toată echipa:**
> - **`Seat`** are două câmpuri noi (nullable): `lockedUntil` (LocalDateTime) și `lockedByUserId` (Long). Un loc **„în coș”** = `status == LOCKED` + `lockedByUserId` = clientul + `lockedUntil` în viitor. Un loc `LOCKED` trebuie să aibă **mereu** `lockedUntil` setat (altfel serviciul dă `NullPointerException`) – inclusiv în `data.sql`.
> - **`service/SeatReservationService`** (Java simplu, `new SeatReservationService()`): `reserve(seat, userId, now)` (blochează 10 minute; aruncă **`SeatUnavailableException`** – extinde `IllegalArgumentException` – cu `SEAT_SOLD` / `SEAT_LOCKED`), `releaseExpiredLocks(seats, now)` (face AVAILABLE locurile cu blocare expirată), `getDisplayStatus(seat, now)`. Constanta `LOCK_DURATION_MINUTES = 10`.
> - **Harta sălii**: `/events/{id}/seats` (`SeatMapController`, `templates/seats.html`). GET afișează harta (eliberează întâi blocările expirate); POST cu `seatId` rezervă locul (doar logat; altfel redirect `/login`). Butonul „Get Tickets” de pe `/events/{id}` duce acum aici. CSS nou: `lt-stage`, `lt-seat`, `lt-seat-available/locked/sold/selected`, `lt-seat-legend`.
> - `data.sql`: locul A2 de la Hamlet (event 3) e în coșul Mariei (user 2) 10 minute de la pornirea aplicației, apoi se eliberează.
> - „Available seats” din catalog / detalii (`countAvailableSeats`) numără doar `AVAILABLE`; blocările expirate devin AVAILABLE în baza de date abia când cineva deschide harta sălii.
> - **Deselectare pe hartă (fix după US-9):** un loc din coșul tău (alb) se poate apăsa din nou ca să fie scos din coș, iar cardul „Your seats” are câte un buton „Remove”. Ambele trimit POST la `/events/{id}/seats/release` (`SeatMapController.releaseSeat`), care refolosește `CartService.removeTicket` (verifică dacă locul e chiar în coșul tău, apoi îl eliberează) – nu există logică nouă de eliberare. Categoria aleasă în coș pentru acel loc se șterge singură din sesiune (`CartController.forgetSeatsNoLongerInCart`).

#### US-8 Limite de cumpărare — responsabil **B** ✅ implementat (T-8.1 – T-8.4)
Ca organizator, vreau limite la cumpărarea biletelor, pentru a preveni revânzarea și accesul minorilor la evenimente interzise lor.
Criterii de acceptare:
1. Maximum 6 bilete pe comandă.
2. La un eveniment sold-out nu se mai pot cumpăra bilete; butonul de cumpărare devine inactiv.
3. La evenimentele 18+ pot cumpăra doar utilizatorii majori (după data nașterii din cont).
4. La depășirea unei limite apare un mesaj care explică motivul.

- **T-8.1 [RED]** – teste `PurchaseLimitsTest`
- **T-8.2 [GREEN]** – implementare `PurchaseLimitsValidator`
- **T-8.3 [REFACTOR]** – constante, mesaje de eroare, metode extrase
- **T-8.4 [UI]** – limitele aplicate pe harta sălii (max 6 locuri în coș, 18+, sold-out) + badge „18+” în catalog și pe pagina evenimentului + eveniment 18+ și utilizator minor în `data.sql`

> ⚠️ **Atenție (lăsat de Ana, US-7) — pentru US-8:**
> - Rezervarea unui loc se face în `SeatMapController.reserveSeat` → `lockSeat(...)`. Limita de 6 bilete (CA1) se poate verifica acolo, înainte de `seatReservationService.reserve(...)`, numărând locurile `LOCKED` cu `lockedByUserId` = clientul curent (vezi `SeatView.mine`). `SeatReservationService` nu are nicio limită de număr.

> ⚠️ **Atenție (lăsat de Ana, US-4):**
> - **`Event`** există deja, dar **nu are câmpul `ageRestriction`** (sau `minAge`). Pentru criteriul 3 (evenimente 18+) va trebui să adaugi acest câmp în `model/Event.java` (ex. `int ageRestriction`, default 0) și să actualizezi `data.sql` cu câteva evenimente 18+ pentru testare — fără să strici evenimentele existente.
> - **`User.birthDate`** (LocalDate) există deja — îl folosești pentru a calcula vârsta utilizatorului.
> - **`EventCatalogService.countAvailableSeats`** există — poți folosi rezultatul pentru criteriul 2 (sold-out).

> ⚠️ **Atenție (lăsat de Maria, US-8) — ce există acum, pentru toată echipa:**
> - **`Event.ageRestriction`** (int, implicit 0 prin `@ColumnDefault("0")`; 18 = doar adulți) cu `getAgeRestriction()` / `setAgeRestriction()`. Evenimentele din `data.sql` nu completează coloana, deci nu au restricție. Pentru un eveniment 18+ adaugă `age_restriction` în `INSERT`.
> - **`service/PurchaseLimitsValidator`** (Java simplu, `new PurchaseLimitsValidator()`):
>   - `validateTicketCount(int ticketCount)`: mai mult de `MAX_TICKETS_PER_ORDER = 6` bilete → excepție `TOO_MANY_TICKETS`.
>   - `isSoldOut(long availableSeats)` și `validateNotSoldOut(long availableSeats)` (`SOLD_OUT`). Numărul de locuri vine din `EventCatalogService.countAvailableSeats`.
>   - `validateAge(Event event, LocalDate birthDate, LocalDate today)`: vârsta în ani împliniți < `ageRestriction` → `AGE_RESTRICTED` (format `%d`). Ziua curentă se primește ca parametru.
>   - Toate aruncă **`PurchaseLimitException`** (extinde `IllegalArgumentException`), deci un `catch (IllegalArgumentException e)` existent afișează deja mesajul.
> - **T-8.4 [UI]**: validatorul e apelat în `SeatMapController.lockSeat`, înainte de `seatReservationService.reserve(...)`: `validateAge`, `validateNotSoldOut` (locuri libere la momentul `now`, blocările expirate contează ca libere) și `validateTicketCount(locurile mele din coș + 1)`. `SeatMapController` are acum în constructor și `UserRepository`. Harta pune în model și `ticketsInCart`, `maxTickets`, `soldOut`, `adultsOnly`.
> - **Limita de 6 e pe comandă = locurile din coș la un eveniment** (locuri `LOCKED` active cu `lockedByUserId` = clientul). La US-9 / US-11, când există comenzi, `CartService` poate refolosi `validateTicketCount(...)` cu numărul de bilete din coș.
> - `/events/{id}` pune în model `soldOut` (din `isSoldOut`) – butonul „Get Tickets” / „Sold Out” îl folosește. Catalogul și pagina de detalii afișează badge-ul `lt-badge-age` („18+”) când `ageRestriction > 0`.
> - `data.sql`: evenimentul 7 **„Late Night Roast”** (STANDUP, București, 18+, 10 locuri) și utilizatorul 3 **Andrei Georgescu** (`andrei@localtix.ro`, născut 2010-03-15, minor, parola `Parola123!`).
> - Testele: `PurchaseLimitsTest` (12 teste, toate verzi).

#### US-9 Coș și finalizare comandă — responsabil **A** ✅ implementat (T-9.1 – T-9.4)
Ca client, vreau să văd biletele alese într-un coș înainte de plată.
Criterii de acceptare:
1. Coșul afișează evenimentul, locurile, categoria fiecărui bilet, prețul pe bilet și totalul.
2. Clientul poate șterge un bilet din coș, iar locul se eliberează.
3. Se afișează timpul rămas din cele 10 minute de rezervare.
4. Butonul „Checkout” duce la pagina de plată.

- **T-9.1 [RED]** – teste `CartServiceTest` (adăugare/ștergere bilet, totalul cu prețurile din `PriceCalculator`, timpul rămas din cele 10 minute; ora curentă primită ca parametru `now`)
- **T-9.2 [GREEN]** – implementare `CartService` (coș ținut în sesiune)
- **T-9.3 [REFACTOR]** – constantă 10 minute, metode extrase, curățare cod
- **T-9.4 [UI]** – pagina coș + cronometru de 10 minute (JS)

> ⚠️ **Atenție (lăsat de David, US-6) — pentru US-9:**
> - Prețul fiecărui bilet din coș se calculează cu **`PriceCalculator.calculate(event.getBasePrice(), ticketCategory, daysBefore)`** – nu rescrie regulile de reducere în `CartService`. Categoria biletului e enum-ul **`TicketCategory`** (NORMAL / STUDENT / SENIOR), afișat cu `getDisplayName()`.
> - În `CartService` primește `now` ca parametru și calculează `daysBefore` din `now` și `event.getDateTime()` (ca în `EventCatalogController.addPrices`), ca să fie testabil.
> - Totalul coșului = suma prețurilor finale (deja rotunjite la 2 zecimale) – folosește `BigDecimal`, nu `double`.
> - Pe pagina coșului poți refolosi stilul tabelului de prețuri: clasele `lt-table`, `lt-badge-discount`, `lt-final-price` din `style.css`.

> ⚠️ **Atenție (lăsat de Ana, US-7) — pentru US-9:**
> - Locurile din coș sunt locurile `LOCKED` cu `Seat.lockedByUserId` = clientul logat (vezi nota de la US-7). Timpul rămas (CA3) = `Seat.lockedUntil` − `now`; durata e `SeatReservationService.LOCK_DURATION_MINUTES` – nu redefini 10 minute în `CartService`.
> - Pentru ștergerea din coș (CA2) `SeatReservationService` **nu are** încă o metodă publică de eliberare a unui singur loc (are doar `unlock` privat, folosit de `releaseExpiredLocks`) – adaug-o tu (ex. `release(seat)`) cu test, fără să schimbi metodele existente.
> - Pagina `/events/{id}/seats` are cardul „Your seats” (locurile din coș cu „Reserved until HH:mm”) – acolo se poate pune butonul spre coș.

> ⚠️ **Atenție (lăsat de David, US-9) — ce există acum, pentru toată echipa:**
> - **Coșul NU e o entitate nouă**: coșul = locurile `LOCKED` cu `lockedByUserId` = clientul și `lockedUntil` în viitor (ca la US-7). Sesiunea ține doar categoria aleasă pentru fiecare loc: cheia `CartController.SESSION_CART_CATEGORIES` (`"cartCategories"`) → `Map<Long seatId, TicketCategory>`; implicit `NORMAL`.
> - **`service/CartService`** (Java simplu, `new CartService()`): `getCartSeats(seats, userId, now)`, `addTicket(eventSeats, seat, userId, now)` (limita de 6 din US-8 + `reserve`), `removeTicket(seat, userId, now)` (aruncă `NOT_IN_CART` dacă locul nu e în coșul clientului; altfel îl eliberează), `calculateTicketPrice(event, category, now)` (prin `PriceCalculator`), `calculateTotal(List<CartItem>)` (2 zecimale), `getRemainingSeconds(cartSeats, now)` (până expiră primul loc, niciodată negativ).
> - **`service/CartItem`** (record): `seat`, `category`, `price` – în Thymeleaf cu paranteze: `${item.price()}`.
> - **`SeatReservationService.release(seat)`** – metodă publică nouă: locul devine `AVAILABLE` și se golesc `lockedUntil` / `lockedByUserId`. Folosiți-o și la rambursare (US-10, CA5).
> - **`SeatRepository.findByStatusAndLockedByUserId(status, userId)`** – locurile unui client de la toate evenimentele.
> - **Pagini**: `/cart` (`CartController`, `templates/cart.html`; POST `/cart/category`, POST `/cart/remove`) și `/checkout` (`templates/checkout.html`; coș gol → redirect `/cart` cu `CART_EMPTY`). Link „Cart” în navbar (doar logat) și buton „Go to cart” pe harta sălii. Cronometrul e în `static/js/cart-timer.js` (citește `data-seconds` de pe `#cart-timer`, la 0 reîncarcă `/cart`). CSS nou: `lt-timer`, `lt-timer-warning`.
> - `SeatMapController` **nu** a fost modificat (în afară de butonul din `seats.html`); el verifică încă singur limitele (US-8) înainte de `reserve`.
> - Testele: `CartServiceTest` (17 teste) + `SeatReservationServiceTest.releasedSeatBecomesAvailable` – toate verzi.

### SPRINT 4 – Plată și bilete

#### US-10 Rambursare — responsabil **C** ✅ implementat (T-10.1 – T-10.3)
Ca client, vreau să pot anula un bilet și să primesc banii înapoi conform regulilor.
Criterii de acceptare:
1. Dacă organizatorul anulează evenimentul, clientul primește 100%.
2. Dacă clientul anulează cu peste 48h înainte, primește 100% minus o taxă de procesare de 10%.
3. Dacă clientul anulează cu mai puțin de 48h înainte, nu primește nimic.
4. După începerea evenimentului biletul nu mai poate fi anulat.
5. După anulare locul redevine disponibil, iar clientul vede suma rambursată.

- **T-10.1 [RED]** – teste `RefundServiceTest`
- **T-10.2 [GREEN]** – implementare `RefundService`
- **T-10.3 [REFACTOR]** – constante (48h, 10%), excepție proprie, metode extrase

> ✅ Nota de mai jos a fost rezolvată la US-10 (`RefundService` eliberează locul cu `SeatReservationService.release`).
>
> ⚠️ **Atenție (lăsat de Ana, US-7) — pentru US-10:**
> - La anulare (CA5), locul redevine disponibil cu `seat.setStatus(SeatStatus.AVAILABLE)` și trebuie golite și `lockedUntil` / `lockedByUserId` (câmpuri adăugate în `Seat` la US-7), ca harta sălii să-l arate liber.

> ⚠️ **Atenție (lăsat de Ana, US-10) — ce există acum, pentru toată echipa:**
> - **`service/RefundService`** (Java simplu, `new RefundService()`): `cancel(Seat seat, BigDecimal paidPrice, boolean cancelledByOrganizer, LocalDateTime now)` → suma rambursată (BigDecimal, 2 zecimale, `HALF_UP`) și **eliberează locul** (prin `SeatReservationService.release`). Reguli: organizatorul anulează → 100%; clientul anulează cu **strict mai mult** de 48h înainte → 90%; altfel (inclusiv la exact 48h) → 0.00 (locul se eliberează oricum). Evenimentul început (`now >= dateTime`) → **`RefundNotAllowedException`** (extinde `IllegalArgumentException`) cu `EVENT_ALREADY_STARTED`; locul nu se atinge.
> - Constante publice: `REFUND_MIN_HOURS_BEFORE = 48`, `PROCESSING_FEE_PERCENT = 10`, `EVENT_ALREADY_STARTED`.
> - **UI-ul pentru anulare se face la T-12.4 [UI]** (pagina „My Tickets”, butonul „Cancel ticket”) – vezi nota de la US-12. `Event` **nu are** un câmp „anulat de organizator” – se trimite ca parametru `cancelledByOrganizer`.
> - Testele: `RefundServiceTest` (12 teste, toate verzi).

#### US-11 Plată online (simulată) — responsabil **A** ✅ implementat (T-11.1 – T-11.4)
Ca client, vreau să plătesc biletele online cu cardul.
Criterii de acceptare:
1. Plata este simulată, fără procesator real.
2. Numărul cardului are 16 cifre și este valid conform algoritmului Luhn.
3. Data de expirare nu e în trecut; CVV-ul are 3 cifre.
4. La plată reușită, comanda devine „Paid” și locurile devin „Sold”.
5. La plată eșuată apare un mesaj de eroare, iar locurile rămân blocate până expiră cele 10 minute.

- **T-11.1 [RED]** – teste `PaymentServiceTest`
- **T-11.2 [GREEN]** – implementare `PaymentService` + schimbarea statusului comenzii și al locurilor
- **T-11.3 [REFACTOR]** – algoritmul Luhn într-o metodă separată, constante (16 cifre, 3 cifre CVV), excepție proprie, metode extrase *(enum-ul `OrderStatus` a fost creat deja la T-11.1, fiindcă testele îl folosesc)*
- **T-11.4 [UI]** – pagina de plată

> ⚠️ **Atenție (lăsat de Ana, US-7) — pentru US-11:**
> - La plată reușită (CA4): `seat.setStatus(SeatStatus.SOLD)` și golește `lockedUntil` / `lockedByUserId`. La plată eșuată (CA5) nu atinge locurile – rămân `LOCKED` și se eliberează singure prin `SeatReservationService.releaseExpiredLocks` după 10 minute.

> ⚠️ **Atenție (lăsat de David, US-9) — pentru US-11:**
> - Pagina de plată **există deja**: `/checkout` (`CartController.showCheckout`, `templates/checkout.html`) – rezumatul comenzii (`items`, `total`), cronometrul și un buton „Pay” **dezactivat** cu alerta „Online card payment will be available soon.”. La T-11.4 înlocuiești alerta și butonul cu formularul de card (nu crea altă pagină) și adaugi POST-ul de plată.
> - Locurile de plătit = `cartService.getCartSeats(seatRepository.findByStatusAndLockedByUserId(LOCKED, userId), userId, now)`; prețul fiecăruia vine din categoria din sesiune (`CartController.SESSION_CART_CATEGORIES`). După plată golește această hartă din sesiune.

> ⚠️ **Atenție (lăsat de David, US-11) — ce există acum, pentru toată echipa:**
> - **`model/Order`** (tabela `orders`): `id`, `userId`, `seats` (`@ManyToMany`, tabela `order_seats`), `total` (BigDecimal), `createdAt`, `status` (**`model/OrderStatus`**: `PENDING` / `PAID`). Constructor `new Order(userId, seats, total, createdAt)` – pornește `PENDING`. **`repository/OrderRepository`** (doar metodele din `JpaRepository`).
> - **`service/PaymentService`** (Java simplu, `new PaymentService()`): `pay(order, card, now)` validează cardul (16 cifre + Luhn, expirare, CVV 3 cifre) și că locurile sunt încă în coșul clientului; la succes locurile devin `SOLD` (fără `lockedUntil` / `lockedByUserId`) și comanda `PAID`. La eșec aruncă **`PaymentException`** (extinde `IllegalArgumentException`) cu `INVALID_CARD_NUMBER` / `CARD_EXPIRED` / `INVALID_CVV` / `RESERVATION_EXPIRED` și nu schimbă nimic. **`service/CardDetails`** (record): `cardNumber`, `expiryMonth`, `expiryYear`, `cvv`.
> - **UI**: formularul de card e pe `/checkout`; POST `/checkout/pay` (`CartController.pay`) salvează locurile și comanda, golește categoriile din sesiune și redirecționează la **`/orders/{id}`** (`OrderController`, `templates/order-confirmation.html` – doar proprietarul comenzii o vede). CSS nou: `lt-badge-paid`. JS nou: `static/js/card-format.js` (afișează numărul cardului în grupuri de 4 cifre).
> - Comanda **nu** reține categoria / prețul fiecărui bilet (doar totalul). Acestea sunt disponibile în `CartController.pay`, în lista `items` (`CartItem`: `seat`, `category`, `price`), **înainte** de golirea sesiunii.
> - Testele: `PaymentServiceTest` (18 teste, toate verzi).

> ⚠️ **Atenție (lăsat de David, US-11) — pentru US-12:**
> - Biletele (`Ticket`) se generează după plata reușită: în `CartController.pay`, după `orderRepository.save(order)`, câte un bilet pentru fiecare `CartItem` din `items` (are locul, categoria și prețul). Numele clientului se ia din `UserRepository.findById(userId)`.
> - **Salvează `item.price()` pe bilet** (ex. câmpul `paidPrice`) – e prețul plătit, de care are nevoie `RefundService.cancel(...)` (vezi nota Anei de mai jos). `Order` reține doar totalul.
> - Pentru „My Tickets” poți adăuga în `OrderRepository` / în repository-ul de bilete metode de tip `findByUserId(...)`.

#### US-12 Bilet electronic — responsabil **B**
Ca client, vreau să primesc în cont un bilet electronic cu cod unic.
Criterii de acceptare:
1. Pentru fiecare bilet plătit se generează un cod unic (ex. `LTX-2026-XXXXXX`).
2. Biletul conține evenimentul, data, locația, locul, numele clientului și codul.
3. Biletele apar în contul clientului, în secțiunea „My Tickets”.
4. Biletele anulate apar marcate „Cancelled”.

- **T-12.1 [RED]** – teste `TicketServiceTest` (format cod `LTX-AAAA-XXXXXX`, coduri unice, bilet anulat marcat „Cancelled”)
- **T-12.2 [GREEN]** – implementare `TicketService` + entitatea Ticket
- **T-12.3 [REFACTOR]** – prefixul și lungimea codului în constante, enum pentru statusul biletului, metode extrase
- **T-12.4 [UI]** – pagina „My Tickets” + buton „Cancel ticket” legat de `RefundService` (UI-ul pentru US-10, vezi nota de mai jos)

> ⚠️ **Atenție (lăsat de Ana, US-10) — pentru US-11 / US-12 – OBLIGATORIU la T-12.4 [UI]:**
> - **US-10 (rambursare) nu are task [UI] propriu**: logica e gata (`RefundService`), dar la momentul US-10 nu existau bilete plătite. **Butonul „Cancel ticket” se face la T-12.4, pe pagina „My Tickets”**, și se leagă de `RefundService`. Fără el, US-10 nu poate fi folosit din aplicație.
> - Ce trebuie făcut la T-12.4:
>   1. Pe fiecare bilet activ din „My Tickets”, un buton **„Cancel ticket”** (POST, ex. `/my-tickets/{ticketId}/cancel`; doar biletele clientului logat). Biletele deja anulate nu au buton.
>   2. Controllerul apelează **`RefundService.cancel(ticket's seat, paidPrice, cancelledByOrganizer, LocalDateTime.now())`** – nu rescrieți regulile de 48h / 10% / eveniment început. `cancelledByOrganizer` = `false` când anulează clientul. Salvați apoi locul (`seatRepository.save(seat)` – `cancel` l-a făcut deja `AVAILABLE`).
>   3. Marcați biletul **„Cancelled”** (US-12, CA4) și salvați suma rambursată pe bilet, ca să rămână vizibilă.
>   4. Afișați suma (US-10, CA5): alertă verde **„Ticket cancelled. Refunded amount: X RON”** (2 zecimale). La **`RefundNotAllowedException`** (eveniment început) – alertă roșie cu `e.getMessage()`.
> - `paidPrice` = prețul final plătit pe bilet (calculat cu `PriceCalculator` la plată) → **salvați prețul plătit pe bilet** la US-11 / US-12, altfel nu se poate calcula rambursarea.
> - Pentru verificare: un eveniment cu mai mult de 48h până la început → 90% din preț; pentru cazul „0.00 RON” adăugați în `data.sql` un eveniment care începe în mai puțin de 48h (ex. `DATEADD(HOUR, 24, LOCALTIMESTAMP)`), ca testul să nu depindă de data zilei.

---

## 7. Rezumat: cine ce task-uri are

| Membru | Story-uri |
|---|---|
| **A** | US-1, US-6, US-9, US-11 |
| **B** | US-2, US-5, US-8, US-12 |
| **C** | US-3, US-4, US-7, US-10 |
