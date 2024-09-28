# Review notes

## Logic notes
This is for general app logic, architecture and code bits found here

### The Good
* Declined permissions is handled - good job!
* `RequestPermissionsLauncher` is provided via service locator - nice!

### The not so Good
* Super unclear what was provided as a library and what is the extension you wrote
    * Usually you are not supposed to change the provided code
    * It's better to create another module that depends on the `cashregister` module and implement the `CashRegister` there

* `Change` class is supposed to be used as a builder
    * E.g. `Change().add(1, 1).add(2, 2).add(5, 1)`
    * Not sure why we always call `.apply { }` on it

* `// magic number should be discussed` <- better extract it to the constant regardless

* app doesn't compile
    * missing `dispatcherProvider` in `GetVenuesUseCase`
    * fixed it manually

* When giving permissions, the app shows infinite loading
    * `Event.PermissionGranted` is happening
        * MainViewModel.kt:37 `venuesUpdatesJob` is NOT canceled when permissions are granted
        * This is why the app is stuck in loading state
        * Amending this to start the collection after permissions are granted fixes the issue

* Why wouldn't we encapsulate permissions requesting logic in the ViewModel?
    * Right now we simply deliver results to it
    * Would be nice to just write `LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.requestPermissions() }`

* I'd separate composables into separate functions
```kotlin
when (val state = viewState.value) {
    is MainViewState.Loading -> LoadingScreen()
    is MainViewState.Content -> ContentScreen(state.venues)
    is MainViewState.Error -> ErrorScreen(state.error)
}

@Composable
fun LoadingScreen() { /* ... */ }


fun ContentScreen(venues: List<Venue>) { /* ... */ }

@Composable
fun ErrorScreen(error: Throwable) { /* ... */ }
```

## Business logic notes
This section reviews the solution and requirements

* `CashRegister.performTransaction { .. }`:
    * "Well, reliable solution is quite slow for this task..." <- is it?
        * Is there a way to speed this one up?
        * If yes, why didn't we do it?
    * I'd remove this comment altogether
* We use `MonetaryElement` as key in the `Change` map
    * This is okay, but isn't the most efficient way - you know why?
    * Hint: has to do with `hashCode` and `equals` methods :)
* `Change.equals`:
    * Is the change equal if the amounts are the same, but the coins are different?
    * I guess it should be, but it's not clear from the requirements
    * Looking at the tests, it seems that it shouldn't be but double check
* `// fun part ---` - I'd remove this comment, this is a little unprofessional
    * Don't get me wrong, it's important to show personality but not in the code itself

### Solution
* Solution seems wrong, let me double check
    * For e.g. 730 EUR change, you initialize the array for 73001 elements
    * Then you try to fill every element with the minimum number of coins?
* The optimal solution for this problem is indeed greedy, but it actually resembles the real life approach
* The solution is to start from the biggest coin and try to fill the change with it
    * If you can't, move to the next biggest coin
    * Repeat until you fill the change
    * ^ This is written by GH Copilot btw 😅
