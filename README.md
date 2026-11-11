# Door – Zustandsautomat einer Tür in Java

Einzelprojekt im Modul *Perfectionnement en Java* (DFHI, Oktober 2026).

Die Klasse `Door` simuliert eine Tür mit PIN-Code. Sie kann geöffnet, geschlossen, verriegelt und entriegelt werden, zählt die Öffnungen und verriegelt sich automatisch, wenn sie geschlossen bleibt. Die Klasse hat keine Konsolenausgabe; ihr Verhalten wird vollständig über JUnit-5-Tests geprüft.

## Zustände und Übergänge

Startzustand: `CLOSED`.

| Ausgangszustand | Aktion | Zielzustand |
|---|---|---|
| CLOSED | `open()` | OPENED (Zähler +1) |
| OPENED | `close()` | CLOSED (Auto-Lock startet) |
| CLOSED | `lock()` | LOCKED |
| LOCKED | `unlock(1234)` | CLOSED (Auto-Lock startet) |
| CLOSED | Auto-Lock nach Ablauf der Zeit | LOCKED |

Jede andere Aktion löst eine `DoorException` mit einem präzisen `ErrorCode` aus:

| Zustand | Aktion | ErrorCode |
|---|---|---|
| OPENED | `open()` | `ALREADY_OPEN` |
| OPENED | `lock()`, `unlock()` | `IS_OPEN` |
| CLOSED | `close()` | `ALREADY_CLOSED` |
| CLOSED | `unlock()` | `NOT_LOCKED` |
| LOCKED | `open()`, `close()` | `IS_LOCKED` |
| LOCKED | `lock()` | `ALREADY_LOCKED` |
| LOCKED | `unlock()` mit falscher PIN | `WRONG_PIN` |

Vor dem Programmieren habe ich die Tür als UML-Zustandsdiagramm modelliert (siehe `Reflexionsbericht.docx`).

## Tests

`DoorTest.java` (JUnit 5) prüft:

- alle gültigen Übergänge (mit `assertDoesNotThrow`),
- das Auto-Lock, auch dessen Abbruch beim Öffnen und den Neustart nach `unlock()`,
- den Öffnungszähler,
- alle ungültigen Aktionen mit parametrisierten Tests (`@ParameterizedTest` + `@MethodSource`), einschließlich des erwarteten `ErrorCode`.

## Ausführen

Das Projekt ist ein IntelliJ-Modul ohne Build-Tool. `Door_Exercise.iml` öffnen, JUnit 5 als Bibliothek einbinden und `DoorTest` ausführen.

## Bekannte Grenzen

- Die Aufgabe verlangt ein Auto-Lock nach **5 Sekunden**. Im Code ist die Verzögerung derzeit auf **500 ms** gesetzt, damit die Tests schnell laufen; sie ist noch nicht konfigurierbar.
- Die PIN (`1234`) steht fest in der Methode `unlock()`.

Beides als Konstante oder Konstruktorparameter auszulagern, ist der nächste Schritt.

## Einsatz von KI

Ich habe einen KI-Assistenten (Claude) genutzt, um die Aufgabe zu verstehen, Java-Konzepte erklären zu lassen und meinen Code zu prüfen. Die Prompts sind im Reflexionsbericht dokumentiert; was übernommen wurde, habe ich entschieden.
