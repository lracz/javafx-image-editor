# 🖼️ JavaFX Image Editor

Egy modern, JavaFX alapú asztali képszerkesztő alkalmazás, amely gazdag eszköztárat kínál a digitális képek manipulálásához. A projekt a **Komputergrafika** egyetemi kurzus keretein belül készült, fókuszálva a pixel-szintű manipulációra és a konvolúciós szűrőkre.

![App Preview](https://placehold.co/1200x700/2c3e50/white?text=JavaFX+Image+Editor+Preview)

## ✨ Főbb Funkciók

- **Alapvető Műveletek**: Képek betöltése és mentése (PNG, JPG, BMP), korlátlan visszavonás (Undo) és újra végrehajtás (Redo).
- **Geometriai Transzformációk**:
  - Vízszintes tükrözés (Flip).
  - 90 fokos forgatás (Rotate).
  - Interaktív zoom (Nagyítás/Kicsinyítés).
- **Csúszka-alapú Korrekciók**:
  - Fényerő (Brightness).
  - Kontraszt (Contrast).
  - Elmosás (Gaussian Blur).
- **Speciális Szűrők**:
  - Fekete-fehér és Szépia (Sepia) effektek.
  - Negatív kép előállítása.
- **Haladó Konvolúciós Szűrők**:
  - **Élkeresés**: Laplace-operátor alapú élkiemelés.
  - **Élesítés**: Unsharp Masking algoritmus.
- **Színcsatornák**: Egyedi R, G, B csatornák kinyerése és megjelenítése.

## 🛠️ Technológiai Stack

- **Nyelv**: Java 17+
- **Framework**: JavaFX
- **UI Stílus**: Egyedi CSS (`style.css`)
- **Algoritmusok**: Native Java PixelReader/PixelWriter, Convolution Matrix processing.

## 🚀 Telepítés és Futtatás

### Előfeltételek:
- **Java JDK 17** vagy újabb.
- **JavaFX SDK** (ha a JDK nem tartalmazza).

### Futtatás:
1. Klónozd a repozitóriót:
   ```bash
   git clone https://github.com/lracz/javafx-image-editor.git
   ```
2. Nyisd meg a projektet az kedvenc IDE-dben (IntelliJ IDEA javasolt).
3. Győződj meg róla, hogy a JavaFX könyvtárak hozzá vannak adva a projekthez.
4. Futtasd a `Main.java` fájlt.

## 📂 Projekt Felépítése

- `src/Main.java`: A teljes alkalmazás logikája, UI definíciója és az algoritmusok.
- `src/style.css`: Az alkalmazás modern, sötét tónusú megjelenéséért felelős stíluslap.
- `lib/`: Szükséges JavaFX és egyéb külső könyvtárak helye.

---
*Készítette: Rácz László (CI880V)*
