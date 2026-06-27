# UnSocial App Help — Features

UnSocial is a personal safety app. This document explains how to use each
feature.

## SOS Alert

The SOS Alert is the core emergency feature. Tap the large red SOS button
on the SOS page (or the SOS quick action on the dashboard) to immediately
send your live GPS location to all your emergency contacts who have
"Notify on SOS" turned on, by email, SMS, and WhatsApp at once. A
confirmation message is also sent to you, the sender, right when you
trigger it — not when it's later resolved. While an SOS is active you can
tap "Update location" to push a fresh GPS reading to your contacts if
you're moving, "I'm safe" to resolve it and let your contacts know you're
okay, or "False alarm" to cancel it if it was triggered by mistake. Only
one SOS alert can be active at a time — resolve or cancel the current one
before triggering a new one. Every alert (active or past) is kept in your
SOS history with a timestamp and status.

## Fake Call

Fake Call lets you trigger a realistic incoming call screen to give
yourself a believable excuse to leave an uncomfortable situation. Go to
the Fake Call page to create a template: set a caller name (e.g. "Mom",
"Boss"), an optional phone number to display, a delay in seconds before it
starts ringing (so you have time to put your phone away first), and a
ringtone. There are four built-in ringtones — Classic Ring, Pulse, Chime,
and Digital Beep — synthesized directly in the browser, so they work
instantly with no audio files to download. Preview any ringtone with the
speaker icon before saving. Mark a template "default" so the "Trigger fake
call now" button on the dashboard and Fake Call page uses it without
asking which one. You can also trigger any specific template directly from
its row using the lightning-bolt icon. When the call comes in, tap either
the red (decline) or green (accept) button to dismiss it — both just close
the screen, since it isn't a real call.

## Fake Message

Fake Message shows a floating notification banner styled like a text
message, which can help you appear to have an urgent reason to step away
or end a conversation. Create a template with a sender name and message
text on the Fake Message page, then trigger it the same way as a fake
call — either the default template from the dashboard, or any specific
one from its row.

## Live Tracking

Live Tracking generates a shareable link that shows your real-time GPS
location to whoever you send it to, for a set duration. Start a tracking
session from the Tracking page; it gives you a unique link (a UUID-based
share token) that doesn't require the recipient to have an UnSocial
account. Useful for letting a friend or family member follow your journey
home, on a date, or during a solo trip. You can end a tracking session
early at any time.

## Safety Timer

Safety Timer is a check-in countdown: set a duration (e.g. "notify my
contacts if I haven't checked in within 30 minutes"), and if you don't
check in before time runs out, UnSocial automatically notifies your
emergency contacts — the same way an SOS alert does, by email, SMS, and
WhatsApp — including any note you added when you started the timer (for
example, "walking home from the station"). This is useful for situations
like a first date, a solo hike, or a late commute where you want a
safety net without needing to actively trigger anything if everything
goes fine. Just open the Timer page and tap "check in" before it expires
to cancel the alert.

## Emergency Contacts

Before any of the alert features are useful, add the people you want
notified. Go to the Contacts page and add a name, phone number,
relationship, and — strongly recommended — an email address. Phone
numbers are used for both SMS and WhatsApp alerts; email is used for
email alerts. Each contact has a "Notify on SOS" toggle (on by default) —
turn it off for a contact you want saved for reference but don't want
woken up by every alert. You can mark one contact as "primary" for your
own reference, edit or remove contacts at any time, and there's no limit
beyond what's reasonable for an emergency contact list (typically a
handful of trusted people).

## Account & Theme

Sign up with your name, email, phone number, and a password (hashed with
BCrypt, never stored in plain text). UnSocial uses light mode by default
for a clean, airy look, but you can switch to dark mode any time with the
theme toggle in the navbar — your preference is remembered on that
device.
