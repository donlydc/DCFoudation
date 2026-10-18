# Claude Prompt — Android UI Style Guide Implementation

## 1. Objective

Implement a reusable Android UI design system based on the following direction:

- Clean
- Familiar
- Modern
- Easy to read
- Rounded
- Production-ready
- Material 3 inspired

The design must feel common and natural to Android users. Avoid experimental, overly decorative, glassmorphism, excessive gradients, or unusual layouts.

Prioritize:

1. Readability
2. Clear visual hierarchy
3. Consistent spacing
4. Reusable components
5. Familiar Android interaction patterns
6. Easy maintenance across the entire application

Do **not** style each screen independently. Build a reusable design system first, then apply it consistently.

---

## 2. General Visual Direction

Use a light, clean interface with a simple blue primary color.

Recommended palette:

```text
Primary              #2563EB
Primary Dark         #1D4ED8
Primary Container    #DBEAFE

Background           #F7F8FA
Surface              #FFFFFF
Surface Variant      #F1F3F5

Text Primary         #111827
Text Secondary       #6B7280
Text Disabled        #9CA3AF

Border               #E5E7EB

Success              #16A34A
Warning              #F59E0B
Error                #DC2626
```

Rules:

- Main background should be a very light neutral gray.
- Cards, dialogs and important surfaces should normally use white.
- Use blue only for primary actions, selection states and important highlights.
- Avoid using too many colors.
- Prefer thin borders and subtle elevation instead of strong shadows.
- Do not use gradients unless there is a very specific reason.
- Do not use oversized decorative UI elements.

---

## 3. Spacing System

Use a consistent spacing scale:

```text
4dp
8dp
12dp
16dp
24dp
32dp
```

Recommended usage:

```text
Screen horizontal padding      16dp
Card internal padding          16dp

Small text spacing             4dp
Label to control               8dp
Component spacing              12dp
Normal block spacing           16dp
Section spacing                24dp
Large section spacing          32dp
```

Do not invent random values such as:

```text
7dp
13dp
19dp
27dp
```

unless required by an existing layout constraint.

---

## 4. Shape System

Use rounded corners consistently.

Create reusable shape tokens such as:

```kotlin
object AppShape {
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val ExtraLarge = RoundedCornerShape(20.dp)
}
```

Recommended usage:

```text
Chip / small control       8dp
Button                    12dp
TextField                 12dp
Icon container            12dp
Card                      16dp
Dialog                    20dp
Bottom sheet              20dp
```

Avoid extremely pill-shaped components everywhere.

Rounded corners should make the interface softer, but the application must still look professional.

---

## 5. Typography

Use a small, consistent typography scale.

Recommended styles:

| Style | Size | Weight | Usage |
|---|---:|---|---|
| Screen Title | 20sp | SemiBold | Screen title |
| Section Title | 16sp | SemiBold | Content section |
| Body Large | 16sp | Regular | Important body text |
| Body | 14sp | Regular | Normal text |
| Button Label | 14sp | SemiBold | Buttons |
| Caption | 12sp | Regular | Secondary information |

Example:

```kotlin
val AppTypography = Typography(
    titleLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    )
)
```

Avoid creating too many slightly different text sizes.

---

# 6. Components To Implement

Build reusable components instead of repeating Material components directly across feature screens.

Suggested package structure:

```text
core/
└── design-system/
    ├── theme/
    │   ├── Color.kt
    │   ├── Typography.kt
    │   ├── Shape.kt
    │   ├── Dimension.kt
    │   └── Theme.kt
    │
    └── component/
        ├── AppButton.kt
        ├── AppText.kt
        ├── AppTopBar.kt
        ├── AppNavigationBar.kt
        ├── AppCard.kt
        ├── AppTextField.kt
        ├── AppIconButton.kt
        ├── AppChip.kt
        ├── AppDialog.kt
        ├── AppBottomSheet.kt
        ├── AppLoading.kt
        ├── AppEmptyState.kt
        └── AppErrorState.kt
```

Feature screens should use these reusable components.

Avoid:

```kotlin
Button(...)
OutlinedButton(...)
TextField(...)
```

directly in many screens with custom styling each time.

Prefer:

```kotlin
AppPrimaryButton(...)
AppSecondaryButton(...)
AppTextField(...)
AppTopBar(...)
AppCard(...)
```

---

# 7. Buttons

Implement at least four button types.

## 7.1 Primary Button

Used for the main action of a screen.

Example:

```text
[ Start Trip ]
[ Save ]
[ Continue ]
[ Confirm ]
```

Specification:

```text
Height       48–52dp
Radius       12dp
Background   Primary
Text         White
Font         14sp SemiBold
```

Prefer full-width primary buttons for important screen-level actions.

---

## 7.2 Secondary Button

Use for secondary actions.

Example:

```text
[ View Details ]
[ Edit ]
[ Retry ]
```

Style:

```text
Background   White / transparent
Border       Primary or neutral border
Text         Primary
Radius       12dp
```

---

## 7.3 Text Button

For low emphasis actions.

Examples:

```text
Cancel
Skip
View all
Learn more
```

No filled background.

---

## 7.4 Destructive Button

For destructive actions.

Examples:

```text
Delete
Reset
End Trip
Log out
```

Use error color carefully.

Do not make every important action red.

---

## 7.5 Button States

Every button must support:

```text
Normal
Pressed
Disabled
Loading
```

Rules:

- Pressed state should be visibly darker or use an interaction overlay.
- Disabled state should reduce emphasis clearly.
- Loading state must keep the same button dimensions.
- Loading state must prevent repeated clicks.
- The text should not jump when the loading indicator appears.

---

# 8. Text Fields

Create a consistent text field component.

Required variants:

```text
Normal
With leading icon
Error
Disabled
Read-only if needed
```

Specification:

```text
Height             52–56dp
Radius             12dp
Background         Surface
Border             neutral
Focused border     Primary
Error border       Error
Horizontal padding 12–16dp
```

Error example:

```text
Label
┌─────────────────────────┐
│ Invalid value         ! │
└─────────────────────────┘
This field is required
```

Avoid strong shadows around input fields.

---

# 9. Top App Bar / Header

Use a common Android Top App Bar pattern.

Example:

```text
┌──────────────────────────────┐
│ ‹   Trip details         ⋮   │
└──────────────────────────────┘
```

Specification:

```text
Height      56dp
Surface     White / surface
Elevation   0 or very subtle
Title       18–20sp SemiBold
Icon        24dp
Touch area  at least 48dp
```

Support:

```text
Navigation icon
Title
Optional subtitle if really needed
Action icons
Overflow menu
```

Do not overload the Top App Bar with too many actions.

Prefer a maximum of 2–3 visible actions.

---

# 10. Section Header

Inside screen content, use a consistent section title.

Example:

```text
Trip information
Fare details
Vehicle information
Payment information
```

Style:

```text
16sp SemiBold
Text Primary
Top spacing      24dp
Bottom spacing   8–12dp
```

Avoid unnecessary all-uppercase text.

---

# 11. Bottom Navigation Bar

Use Bottom Navigation for approximately 3–5 major destinations.

Recommended example:

```text
Home
History
Report
More
```

Specification:

```text
Height          64–72dp
Background      White
Selected        Primary
Unselected      Text Secondary
Icon size       22–24dp
Label           11–12sp
```

Avoid adding too many destinations.

If there are more than 5 major functions, move secondary functions into:

```text
More
Navigation Drawer
Settings
```

---

# 12. Navigation Drawer

Use only when needed.

Drawer example:

```text
Driver App
driver@example.com

Home
Trip History
Reports
Settings

Help & Support
Log out
```

Rules:

- Clear grouped sections.
- Selected item should have a light primary container background.
- Do not place every app action inside the drawer.
- Keep labels concise.

---

# 13. Icon Buttons

Use icon buttons for:

```text
Back
Search
More
Favorite
Delete
Edit
Close
```

Specification:

```text
Container size    40–48dp
Icon size         20–24dp
Radius            12dp or circular
Touch target      minimum 48dp
```

For destructive actions, use a soft error container instead of a fully red square where possible.

---

# 14. Cards / List Items

Cards should be visually light.

Specification:

```text
Background       White
Radius           16dp
Padding          16dp
Border           #E5E7EB
Border width     1dp
Elevation        0–1dp
```

Prefer subtle borders over heavy shadows.

Example:

```text
┌──────────────────────────────────┐
│ 🚕 Trip #10232          245,000 đ│
│    12.6 km · 24 minutes           │
│    Dec 12, 2024  10:24        ›  │
└──────────────────────────────────┘
```

List items should have:

- strong primary information
- weaker secondary information
- predictable alignment
- obvious click area

---

# 15. Chips / Status Tags

Use small chips for status only.

Examples:

```text
Active
Completed
Pending
Cancelled
```

Recommended colors:

```text
Active       Primary
Completed    Success
Pending      Warning
Cancelled    Error
```

Use tinted light backgrounds.

Do not use fully saturated backgrounds for every chip.

---

# 16. Dialog / Modal

Dialogs must be implemented clearly and consistently.

A confirmation dialog should follow this anatomy:

```text
        [ optional icon ]

           End trip?

Are you sure you want to end this trip?
This action cannot be undone.

[ Cancel ]          [ End Trip ]
```

Dialog specification:

```text
Radius                    20dp
Content padding            24dp
Title                      18–20sp SemiBold
Body                       14sp Regular
Title → body spacing       12–16dp
Body → actions spacing     20–24dp
Action spacing             8–12dp
Button height              48dp
```

Dialog rules:

- Keep the title short.
- Explain the action clearly.
- Primary action must be obvious.
- Secondary action should be less emphasized.
- For destructive dialogs, use an error-colored primary action.
- Do not put too much content inside a confirmation dialog.
- Do not use three or four equal-priority actions.

Reusable components can include:

```kotlin
AppConfirmDialog(...)
AppInfoDialog(...)
AppErrorDialog(...)
AppDestructiveDialog(...)
```

---

# 17. Bottom Sheet

Use Bottom Sheet for contextual action lists or compact forms.

Specification:

```text
Top corners      20dp
Background       Surface
Padding          16–24dp
Drag handle      optional
```

Good use cases:

```text
Select payment method
More actions
Select language
Filter options
```

Do not replace every screen with a Bottom Sheet.

---

# 18. Loading State

Create reusable loading states.

Examples:

```text
Inline loading
Button loading
Page loading
Skeleton loading if appropriate
```

Rules:

- Do not block the entire app unnecessarily.
- Show loading near the content being loaded.
- Use full-screen loading only for initial or blocking operations.
- Prevent duplicate actions while loading.

---

# 19. Empty State

Create a reusable empty state.

Example:

```text
[ simple icon ]

No trips yet

Your completed trips will appear here.

[ Start Trip ]
```

Requirements:

- Clear explanation
- Optional action
- Avoid decorative oversized illustrations unless they add meaning

---

# 20. Error State

Create a reusable error state.

Example:

```text
Unable to load trip history

Check your internet connection and try again.

[ Retry ]
```

Rules:

- Explain what failed.
- Provide a recovery action when possible.
- Do not expose raw exceptions to users.

---

# 21. Example Screen — Trip Details

Implement a screen visually similar to:

```text
┌─────────────────────────────────────┐
│ ‹   Trip details                ⋮   │
├─────────────────────────────────────┤
│                                     │
│ Trip information                    │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Distance              12.45 km  │ │
│ │ Duration              00:24:35  │ │
│ │ Waiting time          00:02:12  │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Fare details                        │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Base fare             35,000 đ  │ │
│ │ Waiting fee            2,000 đ  │ │
│ │ ─────────────────────────────── │ │
│ │ Total                 37,000 đ  │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │            END TRIP             │ │
│ └─────────────────────────────────┘ │
│                                     │
├─────────────────────────────────────┤
│   Home      History   Report   More │
└─────────────────────────────────────┘
```

Important:

- Strong information hierarchy
- Numbers aligned clearly
- Main action obvious
- Screen should not feel crowded
- Important fare information must be easy to scan

---

# 22. Example Screen — Trip History

Example:

```text
┌─────────────────────────────────────┐
│ ☰   Trip History                🔍  │
├─────────────────────────────────────┤
│                                     │
│ [All] [Completed] [Pending]         │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Trip #10232          245,000 đ  │ │
│ │ Dec 12 · 10:24     12.6 km     │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Trip #10231          180,000 đ  │ │
│ │ Dec 12 · 08:12      8.4 km     │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Trip #10230          320,000 đ  │ │
│ │ Dec 11 · 17:45     15.2 km     │ │
│ └─────────────────────────────────┘ │
│                                     │
├─────────────────────────────────────┤
│   Home      History   Report   More │
└─────────────────────────────────────┘
```

---

# 23. Accessibility

Maintain normal Android accessibility expectations.

Requirements:

- Touch targets should be at least approximately 48dp.
- Do not rely on color alone to communicate state.
- Provide content descriptions for meaningful icons.
- Text contrast must remain readable.
- Avoid tiny secondary text.
- Error states should include text, not only a red border.
- Loading indicators must not be the only indication when the operation significantly changes state.

---

# 24. Responsive Layout

The design should work well on different Android screen sizes.

Do not hardcode layouts for only one phone.

Use:

```text
Modifier.fillMaxWidth()
weight()
WindowSizeClass if required
adaptive padding
max width for dialogs/content where useful
```

For tablets / wide screens:

- Do not stretch forms endlessly.
- Use sensible max width.
- Consider navigation rail or two-column content if needed.

---

# 25. Implementation Rules for Claude

When implementing:

1. Inspect the current project structure before changing UI.
2. Reuse existing architecture where reasonable.
3. Do not rewrite unrelated business logic.
4. Centralize all design tokens.
5. Create reusable components before replacing screens.
6. Prefer Material 3 primitives internally.
7. Do not duplicate colors, typography or corner radius values.
8. Keep components stateless when practical.
9. Hoist state to screen/view model level.
10. Add Preview functions for reusable Compose components.
11. Support normal, disabled, loading and error states.
12. Keep API names simple and predictable.
13. Avoid over-engineering.
14. Do not create abstract layers without a concrete reuse case.
15. Do not modify behavior unless required for the UI refactor.

---

# 26. Recommended Component APIs

Use APIs similar to:

```kotlin
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null
)
```

```kotlin
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
)
```

```kotlin
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
)
```

```kotlin
@Composable
fun AppConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    destructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
)
```

Avoid giant components with dozens of parameters.

Split components when responsibilities become unclear.

---

# 27. Deliverables

After implementation, provide:

- Centralized color tokens
- Typography tokens
- Shape tokens
- Spacing / dimension tokens
- Reusable button components
- Reusable text field
- Reusable Top App Bar
- Reusable Bottom Navigation
- Reusable Card / list item
- Reusable status chips
- Reusable icon button
- Reusable dialog system
- Loading state
- Empty state
- Error state
- At least one complete migrated screen
- Compose Previews for important reusable components

Also provide a short summary of:

1. Files added
2. Files modified
3. Main design decisions
4. Any UI behavior intentionally kept unchanged
5. Remaining screens that should be migrated later

---

# 28. Final Design Principles

The final application should feel:

```text
Common
Clean
Modern
Rounded
Readable
Professional
Consistent
Familiar to Android users
```

The interface must **not** feel:

```text
Over-designed
Experimental
Too colorful
Crowded
Heavy
Inconsistent
Like every screen was designed separately
```

Use this specification as the visual direction for the entire Android application.
