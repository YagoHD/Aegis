# Handoff — Emails de Auth bonitos (Firebase Console)

> Objetivo: que los correos de **verificación** y **restablecer contraseña** dejen de verse feos
> (asuntos raros, enlace crudo, remitente extraño). Todo esto se hace **en la consola de Firebase**,
> no en el código (salvo 1 línea opcional de idioma, ver el final). Proyecto: **`aegis-c1471`**.

## Dónde
Firebase Console → tu proyecto **aegis-c1471** → **Authentication** → pestaña **Templates / Plantillas**.
Verás plantillas para: *Verificación del correo electrónico*, *Restablecimiento de contraseña*,
*Cambio de dirección de correo*. Cada una tiene un ✏️ (editar) arriba a la derecha.

---

## 1) Nombre del remitente + responder-a (arregla el "remitente raro")
En **cualquiera** de las plantillas, arriba, hay ajustes comunes:
- **Nombre del remitente (Sender name):** `Aegis`
  → así el correo llega como **“Aegis”** en vez de una dirección cruda.
- **Responder a (Reply-to):** pon un correo tuyo real (p. ej. `tyagorbt@gmail.com`).
- **Dirección del remitente (From):** se queda en `noreply@aegis-c1471.firebaseapp.com`.
  Cambiarla del todo a algo tipo `hola@tudominio.com` **requiere dominio propio** (ver "Extra" abajo).
  Con solo el **nombre** ya se ve mucho mejor.

Guarda. Estos ajustes aplican a todas las plantillas.

---

## 2) Plantilla: Verificación del correo electrónico
Edita y pega:

- **Asunto:**
  ```
  Verifica tu correo · Aegis
  ```
- **Mensaje (cuerpo):**
  ```
  ¡Hola!

  Gracias por unirte a Aegis. Confirma tu dirección de correo para activar tu cuenta:

  %LINK%

  Si no has creado una cuenta en Aegis, ignora este mensaje.

  — Aegis
  ```

> `%LINK%` lo convierte Firebase en el **botón/enlace de acción** (no pongas la URL cruda; eso era
> lo que se veía como "link raro"). Placeholders válidos: `%LINK%`, `%EMAIL%`, `%DISPLAY_NAME%`,
> `%APP_NAME%`.

---

## 3) Plantilla: Restablecimiento de contraseña
- **Asunto:**
  ```
  Restablece tu contraseña · Aegis
  ```
- **Mensaje (cuerpo):**
  ```
  ¡Hola!

  Hemos recibido una solicitud para restablecer la contraseña de tu cuenta de Aegis (%EMAIL%).
  Pulsa el enlace para elegir una nueva:

  %LINK%

  Si no has solicitado este cambio, ignora este mensaje: tu contraseña no cambiará.

  — Aegis
  ```

---

## 4) Idioma (para que salgan en el idioma del usuario)
Firebase trae plantillas **por defecto ya traducidas** a muchos idiomas. Para que cada usuario reciba
el correo en SU idioma, hay que decírselo a Firebase desde la app (**1 línea de código**):

```kotlin
FirebaseAuth.getInstance().useAppLanguage()   // usa el idioma del dispositivo/app
```

- Con esto, un usuario en español recibe la versión española, uno en francés la francesa, etc.
- ⚠️ Ojo: lo que edites a mano en la consola (pasos 2 y 3) se guarda **por idioma**. La consola
  tiene un selector de idioma de la plantilla: si solo editas el **español**, los demás idiomas
  usarán las plantillas **por defecto de Firebase** (que están bien, aunque genéricas).
- Recomendación: personaliza al menos **Español** e **Inglés**; el resto que use las de Firebase.

> **Claude puede añadir esa línea `useAppLanguage()`** en el sitio donde la app envía los correos
> (registro / recuperar contraseña). Dímelo y lo dejo hecho.

---

## 5) Probar
1. Regístrate con un correo tuyo → revisa el email de verificación (remitente “Aegis”, asunto y
   cuerpo nuevos, botón limpio).
2. En login, "¿Olvidaste tu contraseña?" → revisa el email de reset.
3. Mira también la carpeta de **spam** la primera vez.

---

## Extra (opcional, avanzado) — remitente y enlace con dominio propio
Si algún día quieres que el correo venga de `hola@tudominio.com` y el enlace sea de tu dominio en vez
de `aegis-c1471.firebaseapp.com`:
- **Enlace bonito:** Authentication → Settings → *Authorized domains* + una **acción personalizada**
  (custom action URL) servida por **Firebase Hosting** con tu dominio.
- **Remitente propio:** requiere **SMTP propio** (o la extensión *Trigger Email* con tu proveedor).
- Es bastante más lío; con los pasos 1–4 el correo ya queda limpio y profesional. Dejarlo para más
  adelante.
