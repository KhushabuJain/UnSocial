# UnSocial App Help — Notifications & Troubleshooting

How UnSocial's alert delivery works, and what to check if a contact isn't
receiving alerts.

## How alerts are delivered

When you trigger an SOS, when a Safety Timer expires without check-in, or
when you mark yourself safe, UnSocial sends a notification to every
emergency contact with "Notify on SOS" turned on, over up to three
channels at once: email (an HTML message with a Google Maps link to your
location), SMS (a text message via Twilio), and WhatsApp (a message via
Twilio's WhatsApp channel). A contact with "Notify on SOS" turned off will
not receive any of these — they're excluded from all three channels, not
just one.

## Why a contact might not be getting WhatsApp alerts

WhatsApp delivery through Twilio's developer sandbox requires each
recipient to opt in once before they can receive any message: they send a
short join code (shown in the developer's Twilio console) to the sandbox
WhatsApp number from their own WhatsApp app. Until they've done that,
WhatsApp alerts to that number will silently fail to deliver even though
the app reports the alert as sent — the other channels (email, SMS) are
unaffected. If WhatsApp alerts aren't arriving for anyone, the developer
may also simply not have WhatsApp delivery turned on yet in the backend
configuration.

## Why a contact might not be getting SMS

SMS delivery depends on the backend's SMS provider (Twilio) being
configured and enabled. On a Twilio trial account specifically, SMS can
only be delivered to phone numbers that have been manually verified in
the Twilio console first — this is a Twilio account restriction, not an
UnSocial setting.

## Why a contact might not be getting email

Make sure the contact has an email address saved at all — it's optional
on the Emergency Contacts form, so a contact added with just a phone
number will only ever receive SMS/WhatsApp, never email. If an email
address is saved and still isn't arriving, check the contact's spam
folder first.

## The AI Safety Assistant (this chatbot)

This assistant runs entirely on a local AI model (via Ollama) on the
developer's own machine — no data is sent to an outside AI provider, and
no API key or internet connection to a third-party AI service is needed.
It answers using two kinds of knowledge: general personal safety guidance,
and how-to information about UnSocial's own features. It is not a
substitute for emergency services, the police, a lawyer, a doctor, or a
mental health professional — for anything urgent, use the SOS Alert
feature and contact local emergency services directly.
