<?cs

# print out the yaml nav for the reference docs, only printing the title,
path, and status_text (API level) for each package.

?>
reference:<?cs
if:docs.packages.link ?>
- title: Class Index
  path: /<?cs var:docs.classes.link ?>
  status_text: no-toggle
- title: Package Index
  path: /<?cs var:docs.packages.link ?>
  status_text: no-toggle<?cs
/if ?><?cs
each:page = docs.pages?><?cs
  if:page.type == "package"?>
- title: <?cs var:page.label ?>
  path: /<?cs var:page.link ?>
  status_text: apilevel-<?cs var:page.apilevel ?><?cs
  /if?><?cs
/each ?>
