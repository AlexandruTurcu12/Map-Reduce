	In cadrul acestei teme am creat o clasa Map si o clasa Reduce pe langa clasa Tema2 si am
folosit 2 ExecutorService, cate unul pentru fiecare tip de task.
	Pana sa dau submit task-urilor, citesc numele si numarul de caractere al fisierelor de
input aflate in fisierul de intrare specificat in linia de comanda. Le retin in vectorii fileNames si
docLengths.

	Urmatorul pas este sa creez task-urile de map, cate unul pentru fiecare fragment de text din
fiecare document. In cadrul task-ului, daca nu sunt la inceputul documentului, pornesc de la pozitia
offset-1 pentru a verifica daca ultimul caracter de dinaintea fragmentului este litera. Daca da,
inseamna ca ma aflu in mijlocul unui cuvant, asa ca voi continua sa citesc caractere pana ajung la
un separator.
	Acum incep sa citesc cuvintele valide din cadrul fragmentului. Cand citesc o litera,
incrementez dimensiunea cuvantului actual (variabila word) si adaug litera la stringul currentWord,
care memoreaza cuvantul. Daca ajung la finalul fragmentului dar sunt in mijlocul unui cuvant, continui
sa citesc litere pana ajung la un separator. Cand ajung la finalul fisierului sau al cuvantului, adaug
lungimea celui din urma in dictionarul de cuvinte (dictionary), iar daca este cel mai lung cuvant,
adaug currentWord in lista longestWordsList, apoi reinitializez word si currentWord. La final, in clasa
Tema2, adaug dictionarul si lista in doua liste finale, si anume finalDictionaryList si finalWordsList,
dupa care inchid executorMap, care este ExecutorService-ul pentru aceasta etapa.

	Am ajuns la momentul in care creez cate un task de reduce pentru fiecare fisier de input.
Verificand dictionarul si lista de cuvinte aferente, calculez rangul fiecarui fisier cu formula
indicata in enunt. In cazul in care cuvantul verificat are lungimea maxima din cadrul dictionarului,
memorez cheia (lungimea) in variabila maxLength si valoarea (numarul de aparitii) in variabila
noMaxLength (sau o incrementez, daca nu este primul cuvant de lungime maxima descoperit). La final,
adaug rangul, maxLength si noMaxLength in listele rangList, maxList si noMaxList, dar si numele
fisierului in lista finalFileNames. Pentru a adauga informatiile necesare si a pastra ordinea in
cadrul listelor (adica pe pozitia i din liste sa nu fie, spre exemplu, numele unui fisier si rangul
altui fisier), creez niste obiecte CompletableFuture pe care le completez cu datele obtinute, dupa
care le adaug in liste. Apoi inchid ExecutorService-ul pentru acest pas, si anume executorReduce.

	La final, deoarece trebuie sa scriu informatii in fisierul de output dupa ranguri in ordine
descrescatoare, sortez finalFileNames, rangList, maxList si noMaxList in functie de elementele din
rangList. Apoi scriu datele finale in cadrul fisierului de iesire mentionat in linia de comanda.
