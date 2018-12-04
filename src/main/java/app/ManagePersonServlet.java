package app;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ManagePersonServlet extends HttpServlet {

	// Идентификатор для сериализации/десериализации.
	private static final long serialVersionUID = 1L;

	// Основной объект, хранящий данные телефонной книги.
	private Phonebook phonebook;

	public ManagePersonServlet() {
		// Вызов родительского конструктора.
		super();

		// Создание экземпляра телефонной книги.
		try {
			this.phonebook = Phonebook.getInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Валидация ФИО и генерация сообщения об ошибке в случае невалидных данных.
	private String validatePersonFMLName(Person person) {
		String error_message = "";

		if (!person.validateFMLNamePart(person.getName(), false)) {
			error_message += "Имя должно быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}

		if (!person.validateFMLNamePart(person.getSurname(), false)) {
			error_message += "Фамилия должна быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}

		if (!person.validateFMLNamePart(person.getMiddlename(), true)) {
			error_message += "Отчество должно быть строкой от 0 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}
		return error_message;
		//return "";
	}

	// Валидация телефонного номера и генерация сообщения об ошибке в случае невалидных данных.
	private String validatePhoneNumber(Person person, String number) {
		String error_message = "";

		if (!person.validateNumber(number)) {
			error_message += "Номер должнен иметь длину от 2 до 50 символов и состоять из  цифр, знаков +, - и #.<br />";
		}
		return error_message;
	}


	// Реакция на GET-запросы.
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
		// иначе русский язык при передаче GET/POST-параметрами превращается в "кракозябры".
		request.setCharacterEncoding("UTF-8");
		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в JSP.
		request.setAttribute("phonebook", this.phonebook);
		// Хранилище параметров для передачи в JSP.
		HashMap<String, String> jsp_parameters = new HashMap<String, String>();
		// Диспетчеры для передачи управления на разные JSP (разные представления (view)).
		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
		RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
		RequestDispatcher dispatcher_for_number = request.getRequestDispatcher("/ManagePhoneNumber.jsp");
		// Действие (action) и идентификатор записи (id) над которой выполняется это действие.
		String action = request.getParameter("action");
		String id = request.getParameter("id");
		// Если идентификатор и действие не указаны, мы находимся в состоянии
		// "просто показать список и больше ничего не делать".
		if ((action == null) && (id == null)) {
			request.setAttribute("jsp_parameters", jsp_parameters);
			dispatcher_for_list.forward(request, response);
		}
		// Если же действие указано, то...
		else {
			switch (action) {
				// Добавление записи.
				case "add": {
					// Создание новой пустой записи о пользователе.
					Person empty_person = new Person();
					// Подготовка параметров для JSP.
					jsp_parameters.put("current_action", "add");
					jsp_parameters.put("next_action", "add_go");
					jsp_parameters.put("next_action_label", "Добавить");
					// Установка параметров JSP.
					request.setAttribute("person", empty_person);
					request.setAttribute("jsp_parameters", jsp_parameters);
					// Передача запроса в JSP.
					dispatcher_for_manager.forward(request, response);
					break;
				}
				// Редактирование записи.
				case "edit": {
					// Извлечение из телефонной книги информации о редактируемой записи.
					Person editable_person = this.phonebook.getPerson(id);

					// Подготовка параметров для JSP.
					jsp_parameters.put("current_action", "edit");
					jsp_parameters.put("next_action", "edit_go");
					jsp_parameters.put("next_action_label", "Сохранить");

					// Установка параметров JSP.
					request.setAttribute("person", editable_person);
					request.setAttribute("jsp_parameters", jsp_parameters);

					// Передача запроса в JSP.
					dispatcher_for_manager.forward(request, response);
					break;
				}
				// Удаление записи.
				case "delete": {
					// Если запись удалось удалить...
					if (phonebook.deletePerson(id)) {
						jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
						jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
					}
					// Если запись не удалось удалить (например, такой записи нет)...
					else {
						jsp_parameters.put("current_action_result", "DELETION_FAILURE");
						jsp_parameters.put("current_action_result_label", "Ошибка удаления");
					}

					// Установка параметров JSP.
					request.setAttribute("jsp_parameters", jsp_parameters);

					// Передача запроса в JSP.
					dispatcher_for_list.forward(request, response);
					break;
				}
				// Добавление номера.
				case "addNumber": {
					// Создание новой пустой записи о пользователе.
					Person editable_person1 = this.phonebook.getPerson(id);
					// Подготовка параметров для JSP.
					jsp_parameters.put("current_action", "addNumber");
					jsp_parameters.put("next_action", "addNumber_go");
					jsp_parameters.put("next_action_label", "Добавить номер");

					// Установка параметров JSP.
					request.setAttribute("person", editable_person1);
					request.setAttribute("jsp_parameters", jsp_parameters);

					// Передача запроса в JSP.
					dispatcher_for_number.forward(request, response);
					break;
				}
				// Редактирование номера.
				case "editNumber": {
					// Извлечение из телефонной книги информации о редактируемой записи.
					Person editable_number_person = this.phonebook.getPerson(id);
					// Подготовка параметров для JSP.
					jsp_parameters.put("current_action", "editNumber");
					jsp_parameters.put("next_action", "editNumber_go");
					jsp_parameters.put("next_action_label", "Сохранить номер");
					jsp_parameters.put("phoneID", request.getParameter("phoneID"));
					request.setAttribute("person", editable_number_person);
					request.setAttribute("jsp_parameters", jsp_parameters);
					dispatcher_for_number.forward(request, response);
					break;
				}

				// Удаление номера.
				case "deleteNumber":{
					Person delete_number_person = this.phonebook.getPerson(id);
					String phoneID = request.getParameter("phoneID");
					// Если запись удалось удалить...
					if (phonebook.deleteNumber(delete_number_person, phoneID)) {
						jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
						jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
					}
					else {
                        jsp_parameters.put("current_action_result", "DELETION_FAILURE");
                        jsp_parameters.put("current_action_result_label", "Ошибка удаления");
                    }
					request.setAttribute("jsp_parameters", jsp_parameters);
					dispatcher_for_list.forward(request, response);
					break;
					}
			}
		}
	}


	// Реакция на POST-запросы.
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
		// иначе русский язык при передаче GET/POST-параметрами превращается в "кракозябры".
		request.setCharacterEncoding("UTF-8");
		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в JSP.
		request.setAttribute("phonebook", this.phonebook);
		// Хранилище параметров для передачи в JSP.
		HashMap<String, String> jsp_parameters = new HashMap<String, String>();
		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
		RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
		RequestDispatcher dispatcher_for_number = request.getRequestDispatcher("/ManagePhoneNumber.jsp");

		// Действие (add_go, edit_go) и идентификатор записи (id) над которой выполняется это действие.
		String add_go = request.getParameter("add_go");
		String addNumber_go = request.getParameter("addNumber_go");
		String editNumber_go = request.getParameter("editNumber_go");
		String edit_go = request.getParameter("edit_go");
		String id = request.getParameter("id");

		// Добавление записи.
		if (add_go != null) {
			// Создание записи на основе данных из формы.
			Person new_person = new Person(request.getParameter("name"), request.getParameter("surname"), request.getParameter("middlename"));
			// Валидация ФИО.
			String error_message = this.validatePersonFMLName(new_person);
			// Если данные верные, можно производить добавление.
			if (error_message.equals("")) {
				// Если запись удалось добавить...
				if (this.phonebook.addPerson(new_person)) {
					jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
				}
				// Если запись НЕ удалось добавить...
				else {
					jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка добавления");
				}

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else {
				// Подготовка параметров для JSP.
				jsp_parameters.put("current_action", "add");
				jsp_parameters.put("next_action", "add_go");
				jsp_parameters.put("next_action_label", "Добавить");
				jsp_parameters.put("error_message", error_message);

				// Установка параметров JSP.
				request.setAttribute("person", new_person);
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_manager.forward(request, response);
			}
		}
		// Редактирование записи.
		if (edit_go != null) {
			// Получение записи и её обновление на основе данных из формы.
			Person updatable_person = this.phonebook.getPerson(request.getParameter("id"));
			updatable_person.setName(request.getParameter("name"));
			updatable_person.setSurname(request.getParameter("surname"));
			updatable_person.setMiddlename(request.getParameter("middlename"));

			// Валидация ФИО.
			String error_message = this.validatePersonFMLName(updatable_person);

			// Если данные верные, можно производить добавление.
			if (error_message.equals("")) {
				// Если запись удалось обновить...
				if (this.phonebook.updatePerson(id, updatable_person)) {
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Редактирование записи выполнено успешно");
				}
				// Если запись НЕ удалось обновить...
				else {
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка редактирования");
				}
				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else {
				// Подготовка параметров для JSP.
				jsp_parameters.put("current_action", "edit");
				jsp_parameters.put("next_action", "edit_go");
				jsp_parameters.put("next_action_label", "Сохранить");
				jsp_parameters.put("error_message", error_message);
				// Установка параметров JSP.
				request.setAttribute("person", updatable_person);
				request.setAttribute("jsp_parameters", jsp_parameters);
				// Передача запроса в JSP.
				dispatcher_for_manager.forward(request, response);
			}
		}

		// Редактирование номера.
		if (editNumber_go != null) {
			// Получение записи и её обновление на основе данных из формы.
			Person updateNumberPerson = this.phonebook.getPerson(request.getParameter("id"));
			String phoneID = request.getParameter("phoneID");
			String phoneNumber = request.getParameter("phone");
			// Валидация номера
			String error_message = this.validatePhoneNumber(updateNumberPerson, phoneNumber);
			// Если данные верные, можно производить добавление.
			if (error_message.equals("")) {
				// Если запись удалось обновить...
				if (this.phonebook.updateNumber(updateNumberPerson, phoneID, phoneNumber)) {
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Редактирование номера выполнено успешно");
				}
				// Если запись НЕ удалось обновить...
				else {
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка редактирования");
				}
				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else {
				// Подготовка параметров для JSP.
				jsp_parameters.put("current_action", "editNumber");
				jsp_parameters.put("next_action", "editNumber_go");
				jsp_parameters.put("next_action_label", "Сохранить номер");
				jsp_parameters.put("error_message", error_message);
				// Установка параметров JSP.
				request.setAttribute("person", updateNumberPerson);
				request.setAttribute("jsp_parameters", jsp_parameters);
				// Передача запроса в JSP.
				dispatcher_for_manager.forward(request, response);
			}
		}

		// Добавление номера.
		if (addNumber_go != null) {
			// Получение записи и её обновление на основе данных из формы.
			Person updatable_person = this.phonebook.getPerson(request.getParameter("id"));
			HashMap<String, String> phonesMap = updatable_person.getPhones();
			String phone = request.getParameter("phone");
			// Валидация номера.
			String error_message = this.validatePhoneNumber(updatable_person, phone);
			// Если данные верные, можно производить добавление.
			if (error_message.equals("")) {
				// Если запись удалось обновить...
				if (this.phonebook.addNumber(updatable_person, phone)) {
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Добавление номера выполнено успешно");
				}
				// Если запись НЕ удалось обновить...
				else {
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка добавления номера");
				}
				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);
				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else {
				// Подготовка параметров для JSP.
				jsp_parameters.put("current_action", "addNumber");
				jsp_parameters.put("next_action", "addNumber_go");
				jsp_parameters.put("next_action_label", "Сохранить");
				jsp_parameters.put("error_message", error_message);
				jsp_parameters.put("phoneID", "0");

				// Установка параметров JSP.
				request.setAttribute("person", updatable_person);
				request.setAttribute("jsp_parameters", jsp_parameters);
				request.setAttribute("phoneID", "0");

				// Передача запроса в JSP.
				dispatcher_for_number.forward(request, response);
			}
		}
	}
}
