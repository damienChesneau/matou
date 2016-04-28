Ce projet a été crée en utilisant la librairie graphique swing. Et un layout particulier qui implique d'avoir cette
classe importé dans notre projet. C'est pourquoi dans le dossier 'classes' il y a un dossier 'com' qui n'est pas
supprimé par la target ant clean.
Pour générer un jar exécutable avec la commande 'java -jar matou.jar' rien de plus simple, il suffit d'utiliser la
target ant par défaut.

Ce projet à été conçu avec un client et un serveur dans une application unique. C'est à dire que n'importe quel
client peut crée son propre serveur. Et ensuite partager son adresse à d'autres personnes.

Une fois lancée, l'application permet d'initialisé un serveur sur un numéro de port défini par l'utilisateur.

En dessous de ce bouton d'initialisation d'un serveur, la possiblité est aussi donnée de se connecté soit a son propre
serveur, soit à un serveur distant.