# Changelog

## Version 3

### Clearer feedback for administrators
- Every save, removal and broadcast now returns to its page with a message that says what happened, for example "The price was saved. Farmers can see it now." or "The crop was removed."
- Refreshing the browser after a save no longer asks to resubmit the form, so a crop, price or broadcast can't be created twice by accident (Post/Redirect/Get).
- Removing a crop, market or farmer record that is still linked to other records (such as prices) now shows a friendly message instead of an error page.
- Removing a crop, market or farmer record asks for confirmation first.
- Broadcasting is blocked, with a message, when no prices have been posted today.
- The administrator dashboard shows a prompt to post today's prices when none have been entered yet.

### Price entry
- The date field cannot be set to a future date in the browser, as well as being checked on the server.
- A duplicate price shows the existing price and a single button to replace it.

### Appearance and accessibility
- Keyboard focus is clearly outlined on every link, button and field.
- Buttons and table rows respond to hover, and buttons press in when clicked.
- Tables scroll sideways on small screens instead of breaking the layout; the statistics show two per row on tablets.
- Motion is reduced for users who prefer less animation.

### Unchanged
- Accounts, password recovery, remember-me sign-in, farmer profiles, price validation rules and the database are unchanged.
