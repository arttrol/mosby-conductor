package com.hannesdorfmann.mosby.conductor.sample.create

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.bluelinelabs.conductor.RouterTransaction
import com.hannesdorfmann.mosby.conductor.sample.R
import com.hannesdorfmann.mosby.conductor.sample.create.contactspicker.ContactsPickerController
import com.hannesdorfmann.mosby.conductor.sample.dagger.DaggerTaskCreationComponent
import com.hannesdorfmann.mosby.conductor.sample.dagger.TaskCreationComponent
import com.hannesdorfmann.mosby.conductor.sample.daggerComponent
import com.hannesdorfmann.mosby.conductor.sample.model.tasks.TaskBuilder
import com.hannesdorfmann.mosby.conductor.sample.navigation.changehandlers.ContactsPickerChaneHandler
import com.hannesdorfmann.mosby.conductor.viewstate.MvpViewStateController
import com.jakewharton.rxbinding.widget.textChanges
import rx.Observable
import javax.inject.Inject

/**
 * Controller to create a new Task
 *
 * @author Hannes Dorfmann
 */
class CreateTaskController : CreateTaskView, MvpViewStateController<CreateTaskView, CreateTaskPresenter, CreateTaskViewState>() {

  private lateinit var title: EditText
  private lateinit var description: EditText
  private lateinit var selectedPersonRecyclerView: RecyclerView
  val createTaskComponent: TaskCreationComponent by lazy {
    val component = DaggerTaskCreationComponent
        .builder()
        .controllerComponent(daggerComponent)
        .build()
    component.inject(this@CreateTaskController)
    addLifecycleListener(taskBuilderLifecycleListener)
    component
  }

  @Inject lateinit var taskBuilderLifecycleListener: TaskBuilderLifecycleListener

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    createTaskComponent // will ensure that dagger component will be initilaized lazily.
    // called before TaskBuilderLifecycleListener restores his state
  }

  override fun createPresenter(): CreateTaskPresenter {
    return createTaskComponent.createTaskPresenter()
  }

  override fun createViewState() = CreateTaskViewState()

  override fun onViewStateInstanceRestored(instanceStateRetained: Boolean) {
    // Not needed
  }

  override fun onNewViewStateInstance() {
    showForm()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    val view = inflater.inflate(R.layout.controller_create_task, container, false)

    title = view.findViewById(R.id.title) as EditText
    description = view.findViewById(R.id.description) as EditText
    selectedPersonRecyclerView = view.findViewById(R.id.personRecyclerView) as RecyclerView

    val addPersonButton = view.findViewById(R.id.addPerson)
    val personPickerContainer = view.findViewById(R.id.personPickerContainer) as ViewGroup
    addPersonButton.setOnClickListener {
      val childRouter = getChildRouter(personPickerContainer, null)
      if (!childRouter.hasRootController()) {
        childRouter.setRoot(RouterTransaction.with(ContactsPickerController())
            .popChangeHandler(ContactsPickerChaneHandler())
        )
        childRouter.setPopsLastView(true)
      }
    }

    return view
  }

  override fun showForm() {
    viewState.setShowForm()
    title.isEnabled = true
    description.isEnabled = true
  }

  override fun showLoading() {
    viewState.setShowLoading()
    title.isEnabled = false
    description.isEnabled = false
  }

  override fun showTaskCreated() {
    viewState.setShowTaskCreated()
    router.popCurrentController()
  }

  override fun title(): Observable<String> =
      title.textChanges().map { it.toString() }

  override fun description(): Observable<String> =
      description.textChanges().map { it.toString() }

  override fun setTaskSnapshot(taskSnapshot: TaskBuilder.TaskSnapshot) {

    if (title.text.toString() != taskSnapshot.title) {
      title.setText(taskSnapshot.title)
    }

    if (description.text.toString() != taskSnapshot.description) {
      description.setText(taskSnapshot.description)
    }


  }
}