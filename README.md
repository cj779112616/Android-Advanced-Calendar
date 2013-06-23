Description:
It is very optimized Calendar for Android.
It draws day on canvas avoid using TextViews, GridLayout and etc..
It is very alpha-version.

Roadmap (TODO):
* Merge CalendarView and CalendarGridView. Now this is a sucks. I'm lazy to implement it
* Extract params from XML
* Optimize draw: make calculation of new month faster, cache background of cells (each cycle they are converted from Drawable to Bitmam. We can cache the result)