# Harem Dark: Vládce Temných Dominií

Harem Dark je temně fantasy textová a strategická Android hra postavená na frameworku Jetpack Compose a moderním Kotlinu. Hráč se ujímá role "Pána" – vůdce podsvětí a temného vládce, který rozšiřuje své impérium, verbuje agenty, dobývá domínia a především buduje svůj harém ze zajatých bojovnic, kněžek a aristokratek.

Hra je silně inspirována visual novelami, RPG management systémy a temnou fantasy. Soustředí se na správu zdrojů, morální úpadek, tahové souboje a detailní systém vztahů.

## Hlavní funkce

### 1. Budování a správa harému (Harem Management)
- **Komplexní vlastnosti dívek:** Každá dívka (Concubine) v harému má vlastní statistiky jako *Srdce, Poslušnost, Vlhkost, Loajalita, Strach, Zvrácenost* a další.
- **Fáze zkázanosti (Degradation Phase):** Dívky procházejí fázemi (např. *Zlomená, Vzdorovitá, Posedlá rozkoší*). Interakce tyto vlastnosti formují.
- **Hierarchie a role:** Dívky mohou být jmenovány oblíbenkyněmi, manželkami nebo pracovat na "nájmu" jako vymahačky, tanečnice či špionky.
- **Interakce:** Rozsáhlý systém dialogů a akcí (Tresty, Oslavování, Trénink) ovlivňující jejich loajalitu a strach, čerpající "Sexuální energii" (SE) a "Temnou sílu" (TE).

### 2. Svět a Průzkum Dominií
- **Mapa světa:** Různorodá území (Ruiny starého chrámu, Měsíční přístav, Šlechtické panství, Hostinec, Tábor žoldnéřek) přinášející různé suroviny a kořist.
- **Lov a expedice:** Vysílání výprav k nalezení nových otrokyň a boji s bossy.
- **NPC postavy a Obchodníci:** Ve specifických lokacích lze potkat charaktery (např. "Tajemná obchodnice Krvavá Mary"), které oživují svět.

### 3. Impérium, Město a Podsvětí
- **Správa mafiánských teritorií:** Vymáhání výpalného, verbování agentů (inkasisté, nájemní vrazi) a kontrola nad čtvrtěmi.
- **Alchymistická laboratoř:** Vaření lektvarů, jedů a balzámů z posbíraných surovin.
- **Ekonomika:** Prodej kořisti a zpráva zlaťáků (získaných z výpalného i aukcí).

### 4. Tahový soubojový systém (Turn-Based Combat)
- **Boj s bossy:** Plně animovaný a taktický tahový soubojový systém z pohledu první osoby (nebo profilových karet).
- **Použití lektvarů a zbraní:** Hráč (Pán) může používat předměty z inventáře (Hojivý balzám atd.) a vybírat si z různých dovedností (Obrana, Útok, Temná magie).
- **Stavy a efekty (Buffs/Debuffs):** Například omráčení nebo krvácení.

### 5. Inventář a Systém předmětů
- **Kategorie předmětů:** Dary, bojové lektvary, úkolové relikvie (např. Černá pečeť).
- **Darování:** Dary dívkám v harému zvyšují jejich lásku a poslušnost.

## Herní mechaniky a smyčka

- **Akční body:** Každá smysluplná činnost stojí Sexuální (SE) nebo Temnou (TE) energii.
- **Odpočinek (Další den):** Doplní energii, vypočítá denní příjmy z mafie a nájmů, a posune čas. Může se odehrát "Noční incident".
- **Denní úkoly:** Speciální výzvy zadávané každý den pro zisk extra zlata nebo energie (např. *Vyraz na výpravu*, *Obdaruj dívku*).

## Architektura (Pro vývojáře)
Hra používá architekturu zaměřenou na jeden sdílený reaktivní `StateFlow` obsahující celý svět (třída `GameSave`).
- `GameEngine` funguje jako centrální repozitář pro byznys logiku (souboje, nákupy, interakce) a aktualizuje globální stav, což vede k okamžitému překreslení Compose UI.
- Uživatelské rozhraní je postaveno primárně na Jetpack Compose s vlastním temným (Dark Fantasy) Material 3 tématem s bohatou vizualizací skrze AI-generovanou grafiku (`painterResource`).

## Upozornění
*Tento projekt je fiktivní fantasy hra koncipovaná pro dospělé, pracující s dark fantasy/grimdark tématy.*
