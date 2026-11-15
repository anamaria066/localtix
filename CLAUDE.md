# CLAUDE.md – LocalTix

Utilizatorul va scrie de obicei: **„Sunt [nume], membrul [A/B/C]. Hai să facem task-ul T-x.y.”**
Găsește task-ul în backlog (secțiunea 5), lucrează **doar** la el și respectă regulile TDD (secțiunea 4).

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
├── model        → entități JPA și enum-uri (User, Event, Seat, Ticket, Order, Categorie...)
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
2. **Nu folosi `LocalDateTime.now()` în logica de business.** Ora curentă se primește ca parametru (`LocalDateTime acum`), ca regulile cu 10 minute / 15 minute / 48 de ore / 14 zile să fie testabile.
3. **Clase în engleză** (`PriceCalculator`, `RefundService`), **metode și metode de test în română descriptivă** (`calculeaza(...)`, `studentPrimeste30LaSuta()`), **toate mesajele afișate utilizatorului în engleză**.
4. Sumele de bani se rotunjesc la 2 zecimale.
5. Erorile de business se semnalează prin excepții cu mesaje clare în engleză (afișate în interfață) (ex. `IllegalArgumentException` sau excepții proprii ca `LocNedisponibilException`).
6. Cod simplu și lizibil – membrii echipei trebuie să-l poată explica. Fără over-engineering.
7. După fiecare fază, explică pe scurt și simplu codul scris.
8. Nu face commit/push decât dacă ți se cere. Format mesaj: `T-6.2 [GREEN] implementare PriceCalculator`.

---

## 4. Reguli TDD – OBLIGATORII

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
3. **STOP:** „📸 Rulează testele și fă captura cu codul refactorizat și testele VERZI.”

### [UI] și alte task-uri fără teste
Construiește pagina Thymeleaf și controllerul, folosind serviciile deja implementate. La final spune la ce URL se vede pagina (`http://localhost:8080/...`). **STOP** la finalul task-ului.

Dacă task-ul cerut depinde de ceva ce nu există încă (ex. o entitate), spune-o și fă doar minimul necesar.

---

## 5. Backlog

### SPRINT 1 – Conturi

#### US-1 Creare cont — responsabil **A**
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

#### US-2 Autentificare — responsabil **B**
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

#### US-3 Pagina de profil — responsabil **C**
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

### SPRINT 2 – Catalog evenimente

#### US-4 Listă de evenimente — responsabil **C**
Ca vizitator, vreau să văd lista evenimentelor disponibile, pentru a alege la ce eveniment să merg.
Criterii de acceptare:
1. Pentru fiecare eveniment: titlu, categorie (concert / stand-up / teatru), oraș, locație, dată și oră, preț de pornire, locuri disponibile.
2. Sortare după dată, cele mai apropiate primele.
3. Evenimentele trecute nu apar.
4. Click pe un eveniment deschide pagina de detalii cu descrierea completă.

- **T-4.1 [GREEN]** – entități Event și Seat + repository-uri + `data.sql` cu evenimente de test
- **T-4.2 [UI]** – pagina catalog + pagina de detalii eveniment

#### US-5 Filtrare evenimente — responsabil **B**
Ca vizitator, vreau să filtrez evenimentele după categorie, oraș, dată și preț.
Criterii de acceptare:
1. Filtrele se pot combina (ex. stand-up în București sub 100 lei).
2. Filtrul de dată acceptă un interval.
3. Filtrul de preț acceptă minim și maxim.
4. Fără rezultate se afișează „No events found”.
5. Filtrele se pot reseta dintr-un buton.

- **T-5.1 [RED]** – teste `EventFilterServiceTest`
- **T-5.2 [GREEN]** – implementare `EventFilterService`
- **T-5.3 [UI]** – formular de filtre pe pagina catalog

#### US-6 Calcul preț bilet — responsabil **A**
Ca client, vreau să văd prețul corect al biletului în funcție de categoria mea și de momentul cumpărării.
Criterii de acceptare:
1. Categorii: normal (preț întreg), student (−30%), pensionar (−40%).
2. Early bird: −20% dacă biletul e cumpărat cu mai mult de 14 zile înainte de eveniment.
3. Reducerile nu se cumulează – se aplică doar cea mai mare.
4. Prețul final se rotunjește la 2 zecimale; un preț de bază negativ aruncă excepție.
5. Se afișează prețul inițial, reducerea aplicată și prețul final.

- **T-6.1 [RED]** – teste `PriceCalculatorTest`
- **T-6.2 [GREEN]** – implementare `PriceCalculator.calculeaza(pretBaza, categorie, zileInainte)`
- **T-6.3 [REFACTOR]** – procentele mutate în enum-ul `Categorie`, constante pentru early bird
- **T-6.4 [UI]** – afișare preț inițial / reducere / preț final pe pagina evenimentului

### SPRINT 3 – Cumpărare bilete

#### US-7 Alegere loc în sală — responsabil **C**
Ca client, vreau să îmi aleg locul pe harta sălii.
Criterii de acceptare:
1. Harta arată locurile libere, ocupate și blocate temporar, în culori diferite.
2. Un loc deja vândut nu mai poate fi selectat.
3. Un loc pus în coș se blochează 10 minute pentru ceilalți clienți.
4. Dacă plata nu se face în 10 minute, locul se eliberează automat.
5. Dacă doi clienți aleg același loc, îl primește doar primul; al doilea vede o eroare.

- **T-7.1 [RED]** – teste `SeatReservationServiceTest`
- **T-7.2 [GREEN]** – implementare `SeatReservationService`
- **T-7.3 [REFACTOR]** – enum pentru starea locului (LIBER / BLOCAT / VANDUT), constantă 10 minute, excepție proprie
- **T-7.4 [UI]** – harta sălii (grilă de locuri colorate) + selectare

#### US-8 Limite de cumpărare — responsabil **B**
Ca organizator, vreau limite la cumpărarea biletelor, pentru a preveni revânzarea și accesul minorilor la evenimente interzise lor.
Criterii de acceptare:
1. Maximum 6 bilete pe comandă.
2. La un eveniment sold-out nu se mai pot cumpăra bilete; butonul de cumpărare devine inactiv.
3. La evenimentele 18+ pot cumpăra doar utilizatorii majori (după data nașterii din cont).
4. La depășirea unei limite apare un mesaj care explică motivul.

- **T-8.1 [RED]** – teste `PurchaseLimitsTest`
- **T-8.2 [GREEN]** – implementare `PurchaseLimitsValidator`
- **T-8.3 [REFACTOR]** – constante, mesaje de eroare, metode extrase

#### US-9 Coș și finalizare comandă — responsabil **A**
Ca client, vreau să văd biletele alese într-un coș înainte de plată.
Criterii de acceptare:
1. Coșul afișează evenimentul, locurile, categoria fiecărui bilet, prețul pe bilet și totalul.
2. Clientul poate șterge un bilet din coș, iar locul se eliberează.
3. Se afișează timpul rămas din cele 10 minute de rezervare.
4. Butonul „Checkout” duce la pagina de plată.

- **T-9.1 [GREEN]** – coș în sesiune + calculul totalului folosind `PriceCalculator`
- **T-9.2 [UI]** – pagina coș + cronometru de 10 minute (JS)

### SPRINT 4 – Plată și bilete

#### US-10 Rambursare — responsabil **C**
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

#### US-11 Plată online (simulată) — responsabil **A**
Ca client, vreau să plătesc biletele online cu cardul.
Criterii de acceptare:
1. Plata este simulată, fără procesator real.
2. Numărul cardului are 16 cifre și este valid conform algoritmului Luhn.
3. Data de expirare nu e în trecut; CVV-ul are 3 cifre.
4. La plată reușită, comanda devine „Paid” și locurile devin „Sold”.
5. La plată eșuată apare un mesaj de eroare, iar locurile rămân blocate până expiră cele 10 minute.

- **T-11.1 [RED]** – teste `PaymentServiceTest`
- **T-11.2 [GREEN]** – implementare `PaymentService` + schimbarea statusului comenzii și al locurilor
- **T-11.3 [UI]** – pagina de plată

#### US-12 Bilet electronic — responsabil **B**
Ca client, vreau să primesc în cont un bilet electronic cu cod unic.
Criterii de acceptare:
1. Pentru fiecare bilet plătit se generează un cod unic (ex. `LTX-2026-XXXXXX`).
2. Biletul conține evenimentul, data, locația, locul, numele clientului și codul.
3. Biletele apar în contul clientului, în secțiunea „My Tickets”.
4. Biletele anulate apar marcate „Cancelled”.

- **T-12.1 [GREEN]** – generare cod unic + entitatea Ticket
- **T-12.2 [UI]** – pagina „My Tickets”

---

## 6. Rezumat: cine ce task-uri are

| Membru | Story-uri |
|---|---|
| **A** | US-1, US-6, US-9, US-11 |
| **B** | US-2, US-5, US-8, US-12 |
| **C** | US-3, US-4, US-7, US-10 |
