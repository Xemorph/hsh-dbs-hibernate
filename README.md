# DBS2 Hibernate Bonusaufgabe

<br />

# Table Of Contents
1. [Informationen](#informationen)
2. [Installation](#installation)
3. [Konfiguration](#konfiguration)
4. [Vor- & Nachteile](#vor-und-nachteile)
    1. [Vertikale](#vertikale)
    2. [Horizontale](#horizontale)
    3. [Universelle](#universelle)

<br />

---
## Informationen
Der Aufgabenteil 8.1 wurde mit dem `Program Transformation System (PTS)` realisiert. Als Bibliothek & PTS kam [Spoon](http://spoon.gforge.inria.fr/) für Java zum Einsatz. Sollten Sie Fragen zum `PTS` haben, dann wenden Sie sich per E-Mail an finn.brandes@stud.hs-hannover.de bitte.

---
## Installation
Damit Sie das Projekt erfolgreich kompilieren können, müssen Sie vorher ein paar Abhängigkeiten aus dem Internet herunterladen und selbstgeschriebene kompilieren.

> PTS Installation & Kompilierung

Bitte navigieren Sie mit einem Terminal in den Ordner `dbs2_hibernate_pts` und führen den Folgenden Befehl aus:
```shell
$ mvn install
```
Maven lädt automatisch alle notwendigen Abhängigkeiten aus dem Internet herunter und installiert das Projekt `dbs2_hibernate_pts` in die Lokale Repository `.m2`.

Das Projekt `dbs2_hibernate_pts` ist der sogenannte `"Präprozessor"` für das Hauptprojekt `dbs2_hibernate`.

> Hauptprojekt Installation

Bitte navigieren Sie mit einem Terminal in den Ordner `dbs2_hibernate`. In diesem befindet sich das Hauptprojekt, welches wir jetzt kompilieren möchten. Bevor Sie jedoch das Projekt kompilieren können, müssen einige Änderungen vorgenommen werden.

1. Lokale Installation des OJDBC Treibers<br />
Wir verwenden den OJDBC Treiber aus Modul, die `JAR`-Datei finden Sie in dem Ordner `dbs2_hibernate`. Sie sollten sich bereits in diesem Ordner befinden, falls nicht, dann navigieren Sie mit Hilfe eines Terminals dorthin. Jetzt führen Sie den folgenden Befehl aus, um den OJDBC-Treiber lokal zur Verfügung zu stellen

```shell
$ mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc7 -Dversion=12.1.0.1 -Dpackaging=jar -Dfile=ojdbc7.jar -DgeneratePom=true
```

2. Auswahl der Verehrbung<br />
Öffnen Sie die Datei `pom.xml` mit einem Texteditor Ihrer Wahl und suchen nach dem Eintrag:
```xml
<processorProperty>
  <name>org.nystroem.pts.AppProcessor</name>
  <properties>
    <property>
      <name>partitionierung</name>
      <value>TABLE_PER_CLASS</value>
    </property>
  </properties>
</processorProperty>
```
> Ändern Sie das Feld `value` nach Ihren Wünschen, folgende Veehrbungen stehen Ihnen zur Verfügung
- SINGLE_TABLE
- JOINED
- TABLE_PER_CLASS
> Bitte beachten Sie die Groß- und Kleinschreibung!

3. Kompilierung & Ausführung<br />
Um das Hauptprojekt jetzt zu kompilieren führen Sie den Befehl `mvn install` im Ordner `dbs2_hibernate` aus. Nach erfolgreicher Kompilierung befindet sich in dem Ordner `dbs2_hibernate/target` zwei `JAR`-Dateien. Bitte verwenden Sie die `JAR`-Datei mit dem Tag `jar-with-dependencies`, da diese alle Abhängigkeiten beinhaltet und somit als Abhängigkeit nur `JRE 1.8+` hat.<br />
Das Programm können Sie wie folgt in dem Terminal ausführen, wenn Sie sich noch im Ordner `dbs2_hibernate` befinden
```shell
$ java -jar target/dbs_hibernate-1.0-SNAPSHOT-jar-with-dependencies.jar
```

---
## Konfiguration
Nach der ersten Ausführung erhalten Sie Folgende Ausgabe im Terminal
```shell
$ java -jar target\dbs_hibernate-1.0-SNAPSHOT-jar-with-dependencies.jar
Dez. 10, 2019 3:00:53 PM org.nystroem.dbs.hibernate.core.ApplicationCore info
INFO: Configuration file created. Please, fill out the new configuration file!
$ 
```
Sie müssen jetzt den Ordner `dbs2_hibernate/config` öffnen und dort die Datei `dbs_hibernate.conf` anpassen. Die Datei sieht nach dem ersten Starten wie folgt aus:
```conf
configuration {
    url="jdbc:oracle:thin:@localhost:1521:db01"
    username=""
    password=""
}
```
In dieser Datei tragen Sie Ihre Verbindungsdaten zur Datenbank ein. Danach speicher Sie die Datei und führen den Befehl im Ordner `dbs2_hibernate` erneut aus:
```shell
$ java -jar target/dbs_hibernate-1.0-SNAPSHOT-jar-with-dependencies.jar
```
Jetzt sollte sich die Applikation erfolgreich mit der Datenbank verbinden. Sollten Fehler auftreten, dann kopieren Sie bitte die Konsolenausgabe und senden diese an finn.brandes@stud.hs-hannover.de .

Viel Spaß und vielen Dank fürs Durchlesen :)

---
## Vor- & Nachteile<a name="vor-und-nachteile" />
### Vertikale Partitionierung (JOINED)<a name="vertikale" />

Vorteile | Nachteile
:--- |:---
Objektorientierte Polymorphie wird vollständig unterstützt | Durch Verwendung von Joins wird die Abfrage aufwendiger.
1:1 Entsprechung von Klassen und Tabellen | 
Die Tabellen sind in Normalform | 

### Horizontale Partitionierung (TABLE_PER_CLASS)<a name="horizontale" />

Vorteile | Nachteile
:--- |:---
Da es keine Joins gibt, sind sehr performante Abfragen möglich | aufwendigere Abfragen, da mehrere Select-/Union-Anweisungen gemacht werden müssen
 | | Die objektorientierte Polymorphie ist auf der Datenbank nur schwach ausgeprägt
 | | Nicht in Normalform, da es Schemaredundanzen in Unterklassen gibt
 | | Auf Datenbank gibt es keine expliziten Fremdschlüssel

### Universelle Partitionierung (SINGLE_TABLE)<a name="universelle" />

Vorteile | Nachteile
:--- |:---
Objektorientierte Polymorphie wird vollständig unterstützt | NOT-NULL Constraints  können nicht benutzt werden -> Attribute Unterklassen
Da es keine Joins gibt, sind sehr performante Abfragen möglich | Speicherbedarf für NULL-Werte
Einfaches konzeptionelles Modell | 
Sehr einfache Syntax -> kein Mapping nötig, wenn Minimalfall abgebildet wird | 