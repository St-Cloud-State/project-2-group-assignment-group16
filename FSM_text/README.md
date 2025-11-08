CSCI 430 Group 16 - Cha Vue, Taha Waqar, Natalie Zettles

Warehouse Management System — FSM User Interface
User Manual

This application is an interactive Warehouse Management System implemented using a Finite State Machine (FSM) model. The interface is fully text-based and simulates how different user roles (Client, Clerk, and Manager) interact with the warehouse system.

Starting the Program

To run the program:

javac *.java
java Main


The application will start in the LoginState.

System Overview

The FSM-based interface replaces the older UserInterface.java class with a modular state-driven design.
There are four states in the system:

State	Description
LoginState	Starting point of the system. Users can log in as a Client, Clerk, or Manager.
ClientMenuState	Handles all operations available to logged-in clients.
ClerkMenuState	Handles warehouse clerk operations, including client management and payments.
ManagerMenuState	Handles product management, waitlists, and shipment receiving.

The system transitions between states automatically based on user selections and stored session information (such as the user’s role and client ID).

Navigation & Roles
1. LoginState

The entry point where you select your role.

Options:

Login as Client — requires entering a valid Client ID.

Clerk Menu — for warehouse clerks.

Manager Menu — for managers.

Exit — closes the program.

Depending on your choice:

Clients go to ClientMenuState.

Clerks go to ClerkMenuState.

Managers go to ManagerMenuState.

2. ClientMenuState

Once logged in, the context stores your ClientID.
All actions now relate to that specific client.

Menu Options:

Show client details

Show list of products (with price)

Show client transactions and invoices

Add item to wishlist

Display wishlist

Place an order

Logout

Logout behavior:

If you logged in directly → returns to LoginState

If a Clerk started your session → returns to ClerkMenuState

3. ClerkMenuState

Clerks handle clients and basic warehouse tasks.

Menu Options:

Add new client

Show list of products (with quantity and price)

Show list of all clients

Show clients with outstanding balances

Record payment from a client

Become a client (enter Client ID → switch to ClientMenuState)

Logout

Logout behavior:

If Clerk session started by Manager → returns to ManagerMenuState

Otherwise → returns to LoginState

4. ManagerMenuState

Managers control products and high-level operations.

Menu Options:

Add new product

Display product waitlist

Receive shipment

Become a clerk

Logout

Logout behavior:

Always returns to LoginState

FSM Concept (How It Works)

The Context class controls which state is active.

Each state (Login, Client, Clerk, Manager) implements a shared State interface.

When you make a menu selection, the system transitions to the next state automatically.

The Context remembers:

The entry role (to know where to return after logout)

The current client ID (for client-specific operations)

Example:

LoginState → ClerkMenuState → Become Client → ClientMenuState → Logout → returns to ClerkMenuState

Developer Notes

State.java defines the FSM interface methods: onEnter, run, onExit, and getName.

WarehouseState.java provides a minimal base implementation for all concrete states.

Context.java is the central FSM controller and holds:

The current state

Session memory (role, client ID)

Transition logic (changeState(), logout())

Main.java is now the entry point of the application.

Example Session Flow

Start Program → LoginState:

===== Warehouse System =====
1) Login as Client
2) Clerk Menu
3) Manager Menu
0) Exit
Select option: 2


→ ClerkMenuState

===== Clerk Menu =====
1) Add a client
2) Show products
...
6) Become a client
0) Logout


→ Choose 6 → Enter Client ID → ClientMenuState

===== Client Menu =====
1) Show client details
2) Show list of products
...
0) Logout


→ Logout → returns to ClerkMenuState

===== Clerk Menu =====

Project Structure
src/
├── Client.java
├── ClientList.java
├── ClerkMenuState.java
├── ClientMenuState.java
├── ManagerMenuState.java
├── LoginState.java
├── Warehouse.java
├── WarehouseState.java
├── Context.java
├── State.java
└── Main.java
