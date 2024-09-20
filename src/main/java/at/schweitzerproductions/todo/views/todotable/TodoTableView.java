package at.schweitzerproductions.todo.views.todotable;

import at.schweitzerproductions.todo.data.Todo;
import at.schweitzerproductions.todo.services.TodoService;
import at.schweitzerproductions.todo.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("Todo Table")
@Route(value = "todo-table/:todoID?/:action?(edit)", layout = MainLayout.class)
public class TodoTableView extends Div implements BeforeEnterObserver {

    private final String TODO_ID = "todoID";
    private final String TODO_EDIT_ROUTE_TEMPLATE = "todo-table/%s/edit";

    private final Grid<Todo> grid = new Grid<>(Todo.class, false);

    private DatePicker datum;
    private TextField status;
    private TextField wichtigkeit;
    private TextField dringlichkeit;
    private TextField tags;
    private TextField titel;
    private TextField detailBeschreibung;
    private TextField bearbeitungKommentar;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<Todo> binder;

    private Todo todo;

    private final TodoService todoService;

    public TodoTableView(TodoService todoService) {
        this.todoService = todoService;
        addClassNames("todo-table-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("datum").setAutoWidth(true);
        grid.addColumn("status").setAutoWidth(true);
        grid.addColumn("wichtigkeit").setAutoWidth(true);
        grid.addColumn("dringlichkeit").setAutoWidth(true);
        grid.addColumn("tags").setAutoWidth(true);
        grid.addColumn("titel").setAutoWidth(true);
        grid.addColumn("detailBeschreibung").setAutoWidth(true);
        grid.addColumn("bearbeitungKommentar").setAutoWidth(true);
        grid.setItems(query -> todoService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(TODO_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(TodoTableView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Todo.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.todo == null) {
                    this.todo = new Todo();
                }
                binder.writeBean(this.todo);
                todoService.update(this.todo);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(TodoTableView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> todoId = event.getRouteParameters().get(TODO_ID).map(Long::parseLong);
        if (todoId.isPresent()) {
            Optional<Todo> todoFromBackend = todoService.get(todoId.get());
            if (todoFromBackend.isPresent()) {
                populateForm(todoFromBackend.get());
            } else {
                Notification.show(String.format("The requested todo was not found, ID = %s", todoId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(TodoTableView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        datum = new DatePicker("Datum");
        status = new TextField("Status");
        wichtigkeit = new TextField("Wichtigkeit");
        dringlichkeit = new TextField("Dringlichkeit");
        tags = new TextField("Tags");
        titel = new TextField("Titel");
        detailBeschreibung = new TextField("Detail Beschreibung");
        bearbeitungKommentar = new TextField("Bearbeitung Kommentar");
        formLayout.add(datum, status, wichtigkeit, dringlichkeit, tags, titel, detailBeschreibung,
                bearbeitungKommentar);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Todo value) {
        this.todo = value;
        binder.readBean(this.todo);

    }
}
