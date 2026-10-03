# GithubBackupApp
Aplicación de escritorio desarrollada en Java con interfaz gráfica (Swing) diseñada para automatizar el respaldo de múltiples proyectos o carpetas locales hacia organizaciones de GitHub de forma rápida y ordenada.

LA APP INCLUYE:
Escaneo automático: Detecta todas las subcarpetas del directorio donde se ejecuta.

Inicialización inteligente: Si una carpeta no tiene Git, inicializa un repositorio local (git init), crea un commit inicial y genera un repositorio privado en la organización indicada en GitHub.

Sincronización continua: Si la carpeta ya cuenta con repositorio, detecta cambios nuevos (git status), genera un commit automático con fecha/hora y hace un push a la nube.

Gestión de errores: Si una organización no existe o hay problemas de permisos, captura el mensaje real devuelto por la CLI de GitHub y lo reporta claramente en los logs sin romper la aplicación.


Para que la aplicación pueda interactuar con GitHub, la PC donde se ejecute debe tener instalados:
Git (para el control de versiones local).
GitHub CLI (gh) (para la creación automática de repositorios remotos).
Ademas de que al descargar github CLI tiene que iniciar sesion en una terminal con el comando
gh auth loggin


GUIA DE USO:
Descarga y descomprime la carpeta de la aplicación (GitHubBackup).

Haz doble clic en el archivo ejecutable GitHubBackup.exe.


En la interfaz gráfica:
Ingresa el nombre de tu Organización de GitHub.

Ingresa tu Nombre y Correo configurados en Git.

Haz clic en Guardar Configuración.

Coloca el ejecutable (o mueve la aplicación) dentro de la carpeta principal que contiene todos los proyectos/carpetas que deseas respaldar.

Haz clic en Iniciar Respaldo de Carpetas y observa el progreso en la consola de registros.
