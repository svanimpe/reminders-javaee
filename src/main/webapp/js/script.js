// Replace this URL with the URL where the back-end is hosted.
var BASE_URL = "http://localhost:8080/reminders/api";
var USERNAME = "admin";
var PASSWORD = "supersecret";

var lists = [];
var reminders = [];

var selectedListIndex = undefined;
var editingList = false;

var selectedReminderIndex = undefined;
var editingReminder = false;

var map = undefined;
var marker = undefined;

onload = function() {
    
    initialiseLists();
    prepareListDialog();
    prepareReminderDialog();
    prepareMapDialog();
};

function initialiseLists() {
    
    // Load the lists from the back-end.
    var request = new XMLHttpRequest();
    request.open("GET", BASE_URL + "/lists", true, USERNAME, PASSWORD);
    request.onload = function() {
        if (request.status === 200) {
            lists = JSON.parse(request.responseText);
            for (var i = 0; i < lists.length; i++) {
                $("#listList").append(createListElementForList(i));
            }
            
            if (lists.length > 0) {
                selectListAndLoadReminders(0);
            } else {
                $(".reminderDialogToggle").attr("disabled", true);
            }
        } else {
            console.log("Error loading lists: " + request.status + " - "+ request.statusText);
        }
    };
    request.send(null);
}

/* Lists */

function prepareListDialog() {
    
    // This dialog can be used both for adding and editing lists.
    // By default it is set up for adding a new list.
    // When the dialog is closed, it is reset back to its default.
    
    $("#listDialog .alert-error").hide();    
    $("#listDialogDelete").hide();
    
    $("#listDialog").on("hidden", function() {
        $("#listDialog h3").text("Add List");
        $("#listTitle").val("");
        $("#listDialog .alert-error").hide();
        $("#listDialogDelete").hide();
    });
    
    $("#listDialog").on("shown", function() {
        $("#listTitle").focus();
    });
    
    $(".listDialogToggle").click(function() {
        editingList = false;
        $("#listDialog").modal("show");
    });
    
    $("#listDialogDelete").click(function() {
        deleteSelectedList();
    });
    
    $("#listDialogCancel").click(function() {
        $("#listDialog").modal("hide");
    });
    
    $("#listDialogSave").click(function() {
        if (editingList) {
            updateListWithInput();
        } else {
            createListFromInput();
        }
    });
    
    $("#listTitle").keydown(function(event) {
        if(event.keyCode === 13){
            event.preventDefault();
            $("#listDialogSave").click();
        }
    });
}

function selectListAndLoadReminders(listIndex, selectedReminderId) {
    
    selectedListIndex = listIndex;
    $("#listList li").removeClass("active");
    $("#listList .icon-edit").hide();

    var selectedElement = $("#listList li")[selectedListIndex];
    $(selectedElement).addClass("active");
    $(".icon-edit", selectedElement).show();

    $("#reminderList").empty();

    // Load the reminders in this list from the back-end.
    var request = new XMLHttpRequest();
    request.open("GET", BASE_URL + "/lists/" + lists[selectedListIndex].id + "/reminders", true, USERNAME, PASSWORD);
    request.onload = function() {
        if (request.status === 200) {
            reminders = JSON.parse(request.responseText);
            for (var i = 0; i < reminders.length; i++) {
                reminders[i].list = lists[selectedListIndex].id;
                $("#reminderList").append(createListElementForReminder(i));
            }
            
            if (selectedReminderId !== undefined) {
                for (var i = 0; i < reminders.length; i++) {
                    if (reminders[i].id === selectedReminderId) {
                        selectReminder(i);
                        break;
                    }
                }
            } else if (reminders.length > 0) {
                selectReminder(0);
            }
        } else {
            console.log("Error loading reminders: " + request.status + " - " + request.statusText);
        }
    };
    request.send(null);
}

function createListFromInput() {
    
    var list = {};
    list.title = jQuery.trim($("#listTitle").val());
    
    if (list.title.length < 1) {
        $("#listDialog .alert-error").text("A list's title cannot be empty").show();
        return;
    }
    
    for (var i = 0; i < lists.length; i++) {
        if (list.title === lists[i].title) {
            $("#listDialog .alert-error").text("A list with this title already exists").show();
            return;
        }
    }
    
    // Send the new list to the back-end.
    var request = new XMLHttpRequest();
    request.open("POST", BASE_URL + "/lists", true, USERNAME, PASSWORD);
    request.onload = function() {
        if (request.status === 201) {
            list.id = parseInt(request.getResponseHeader("Location").split("/").pop());
            list.size = 0;
            lists.push(list);
            $("#listList").append(createListElementForList(lists.length - 1));
            selectListAndLoadReminders(lists.length - 1);
            $(".reminderDialogToggle").attr("disabled", false);
            $("#listDialog").modal("hide");
        } else {
            $("#listDialog .alert-error").text("Error creating list. See the console for more information.").show();
            console.log("Error creating list: " + request.status + " " + request.responseText);
        }
    };
    request.setRequestHeader("Content-Type", "application/json");
    request.send(JSON.stringify(list));
}

function updateListWithInput() {
    
    var list = jQuery.extend(true, {}, lists[selectedListIndex]);
    list.title = jQuery.trim($("#listTitle").val());
    
    if (list.title.length < 1) {
        $("#listDialog .alert-error").text("A list's title cannot be empty").show();
        return;
    }
    
    for (var i = 0; i < lists.length; i++) {
        if (i !== selectedListIndex && list.title === lists[i].title) {
            $("#listDialog .alert-error").text("A list with this title already exists").show();
            return;
        }
    }

    // Send the updated list to the back-end.
    var request = new XMLHttpRequest();
    request.open("PUT", BASE_URL + "/lists/" + list.id, true, USERNAME, PASSWORD);
    request.onload = function() {
        if (request.status === 204) {
            lists.splice(selectedListIndex, 1, list);
            var oldElement = $("#listList li")[selectedListIndex];
            $(oldElement).replaceWith(createListElementForList(selectedListIndex));
            var newElement = $("#listList li")[selectedListIndex];
            $(newElement).addClass("active");
            $(".icon-edit", newElement).show();
            $("#listDialog").modal("hide");
        } else {
            $("#listDialog .alert-error").text("Error creating list. See the console for more information.").show();
            console.log("Error creating list: " + request.status + " " + request.responseText);
        }
    };
    request.setRequestHeader("Content-Type", "application/json");
    request.send(JSON.stringify(list));
}

function deleteSelectedList() {
    
    // Send a delete request to the back-end.
    var request = new XMLHttpRequest();
    request.open("DELETE", BASE_URL + "/lists/" + lists[selectedListIndex].id, true, USERNAME, PASSWORD);
    request.onload = function() {
        if (request.status === 204) {
            lists.splice(selectedListIndex, 1);
            
            // Rebuild the list list (otherwise the indices used in the list elements are off).
            $("#listList").empty();
            for (var i = 0; i < lists.length; i++) {
                $("#listList").append(createListElementForList(i));
            }
            
            if (selectedListIndex > 0) {
                selectListAndLoadReminders(selectedListIndex - 1);
            } else if (lists.length > 0) {
                selectListAndLoadReminders(0);
            } else {
                selectedListIndex = undefined;
                $(".reminderDialogToggle").attr("disabled", true);
            }
            $("#listDialog").modal("hide");
        } else {
            console.log("Error deleting list: " + request.status + " - " + request.statusText);
        }
    };
    request.send(null);
}

function createListElementForList(listIndex) {
    
    var editIcon = $("<i>")
        .addClass("icon-edit icon-large pull-right")
        .click(function(event) {
            event.stopPropagation();
            
            // Prepare the dialog for editing instead of adding.
            editingList = true;
            $("#listDialog h3").text("Edit List");
            $("#listTitle").val(lists[listIndex].title);
            $("#listDialogDelete").show();
            $("#listDialog").modal("show");
        });
    
    var link = $("<a>")
        .text(lists[listIndex].title)
        .append(editIcon);
    
    return $("<li>")
        .append(link)
        .click(function() {
            selectListAndLoadReminders(listIndex);
        });
}

/* Reminders */

function prepareReminderDialog() {
    
    // This is all pretty similar to the list dialog.
    
    $("#datePicker").datetimepicker({
        pickSeconds: false
    });
    
    $("#reminderDialog .alert-error").hide();
    $("#reminderDialogDelete").hide();
    
    $("#reminderDialog").on("hidden", function() {
        $("#reminderDialog h3").text("Add Reminder");
        $("#reminderTitle").val("");
        $("#reminderDialog .alert-error").hide();
        $("#reminderDialogDelete").hide();
    });
    
    // Populate the list select when the dialog is shown.
    $("#reminderDialog").on("shown", function() {
        $("#reminderListSelect").empty();
        for (var i = 0; i < lists.length; i++) {
            $("#reminderListSelect").append(createSelectOptionForList(i));
        }
        $("#reminderTitle").focus();
    });
    
    $(".reminderDialogToggle").click(function() {
        editingReminder = false;
        
        // Set the date to the current date and time by default.
        var now = new Date();
        now.setSeconds(0);
        now.setMilliseconds(0);
        $("#datePicker").data("datetimepicker").setLocalDate(now);
        $("#reminderDialog").modal("show");
    });
    
    $("#reminderDialogDelete").click(function() {
        deleteSelectedReminder();
    });
    
    $("#reminderDialogCancel").click(function() {
        $("#reminderDialog").modal("hide");
    });
    
    $("#reminderDialogSave").click(function() {
        if (editingReminder) {
            updateReminderWithInput();
        } else {
            createReminderFromInput();
        }
    });
    
    $("#reminderTitle").keydown(function(event) {
        if(event.keyCode === 13){
            event.preventDefault();
            $("#reminderDialogSave").click();
        }
    });
}

function selectReminder(reminderIndex) {
    
    selectedReminderIndex = reminderIndex;
    $("#reminderList li").removeClass("active");
    $("#reminderList .icon-edit").hide();
    $("#reminderList .icon-map-marker").hide();
    $("#reminderList .icon-trash").hide();

    var selectedElement = $("#reminderList li")[selectedReminderIndex];
    $(selectedElement).addClass("active");
    $(".icon-edit", selectedElement).show();
    $(".icon-map-marker", selectedElement).show();
    $(".icon-trash", selectedElement).show();
}

function createReminderFromInput() {
    
    var reminder = {};
    var listIndex = parseInt($("#reminderListSelect").val());
    reminder.list = lists[listIndex].id;
    reminder.title = jQuery.trim($("#reminderTitle").val());
    if ($("#datePicker").data("datetimepicker").getLocalDate()) {
        reminder.date = $("#datePicker").data("datetimepicker").getLocalDate().getTime();
    }
    
    if (reminder.title.length < 1) {
        $("#reminderDialog .alert-error").text("A reminder's title cannot be empty").show();
        return;
    }
    
    // Send the new reminder to the back-end.
    var request = new XMLHttpRequest();
    request.open("POST", BASE_URL + "/lists/" + reminder.list + "/reminders", true, USERNAME, PASSWORD);
    request.onload = function() {
        if (request.status === 201) {
            reminder.id = parseInt(request.getResponseHeader("Location").split("/").pop());
            if (listIndex !== selectedListIndex) {
                selectListAndLoadReminders(listIndex, reminder.id);
            } else {
                reminders.push(reminder);
                $("#reminderList").append(createListElementForReminder(reminders.length - 1));
                selectReminder(reminders.length - 1);
            }
            $("#reminderDialog").modal("hide");
        } else {
            $("#reminderDialog .alert-error").text("Error creating reminder. See the console for more information.").show();
            console.log("Error creating reminder: " + request.status + " " + request.responseText);
        }
    };
    request.setRequestHeader("Content-Type", "application/json");
    request.send(JSON.stringify(reminder));
}

function updateReminderWithInput() {
    
    var reminder = jQuery.extend(true, {}, reminders[selectedReminderIndex]);
    var listIndex = parseInt($("#reminderListSelect").val());
    reminder.list = lists[listIndex].id;
    reminder.title = jQuery.trim($("#reminderTitle").val());
    if ($("#datePicker").data("datetimepicker").getLocalDate()) {
        reminder.date = $("#datePicker").data("datetimepicker").getLocalDate().getTime();
    } else {
        reminder.date = null;
    }
    
    if (reminder.title.length < 1) {
        $("#reminderDialog .alert-error").text("A reminder's title cannot be empty").show();
        return;
    }
    
    // Send the updated reminder to the back-end.
    var request = new XMLHttpRequest();
    request.open("PUT", BASE_URL + "/lists/" + reminders[selectedReminderIndex].list + "/reminders/" + reminder.id, true, USERNAME, PASSWORD);
    request.onload = function() {
        if (request.status === 204) {
            if (listIndex !== selectedListIndex) {
                selectListAndLoadReminders(listIndex, reminder.id);
            } else {
                reminders.splice(selectedReminderIndex, 1, reminder);
                var oldElement = $("#reminderList li")[selectedReminderIndex];
                $(oldElement).replaceWith(createListElementForReminder(selectedReminderIndex));
                selectReminder(selectedReminderIndex);
            }
            $("#reminderDialog").modal("hide");
        } else {
            $("#reminderDialog .alert-error").text("Error creating reminder. See the console for more information.").show();
            console.log("Error creating reminder: " + request.status + " " + request.responseText);
        }
    };
    request.setRequestHeader("Content-Type", "application/json");
    request.send(JSON.stringify(reminder));
}

function deleteSelectedReminder() {
    
    // Send a delete request to the back-end.
    var request = new XMLHttpRequest();
    request.open("DELETE", BASE_URL + "/lists/" + lists[selectedListIndex].id + "/reminders/" + reminders[selectedReminderIndex].id, true, USERNAME, PASSWORD);
    request.onload = function() {
        if (request.status === 204) {
            reminders.splice(selectedReminderIndex, 1);
            
            // Rebuild the reminder list (otherwise the indices used in the list elements are off).
            $("#reminderList").empty();
            for (var i = 0; i < reminders.length; i++) {
                $("#reminderList").append(createListElementForReminder(i));
            }
            
            if (selectedReminderIndex > 0) {
                selectReminder(selectedReminderIndex - 1);
            } else if (reminders.length > 0) {
                selectReminder(0);
            } else {
                selectedReminderIndex = undefined;
            }
            $("#reminderDialog").modal("hide");
        } else {
            console.log("Error deleting reminder: " + request.status + " - " + request.statusText);
        }
    };
    request.send(null);
}

function createListElementForReminder(reminderIndex) {
    
    var editIcon = $("<i>")
        .addClass("icon-edit icon-large pull-right")
        .click(function(event) {
            event.stopPropagation();
            
            // Prepare the dialog for editing instead of adding.
            editingReminder = true;
            $("#reminderDialog h3").text("Edit Reminder");
            $("#reminderTitle").val(reminders[reminderIndex].title);
            if (reminders[reminderIndex].date) {
                $("#datePicker").data("datetimepicker").setLocalDate(new Date(reminders[reminderIndex].date));
            } else {
                $("#datePicker").data("datetimepicker").setLocalDate(null);
            }
            $("#reminderDialogDelete").show();
            $("#reminderDialog").modal("show");
        });
    
    var mapIcon = $("<i>")
        .addClass("icon-map-marker icon-large pull-right")
        .click(function(event) {
            event.stopPropagation();
            $("#mapDialog").modal("show");
        });
    
    if (reminders[reminderIndex].location) {
        mapIcon.addClass("icon-red");
    }
    
    var deleteIcon = $("<i>")
        .addClass("icon-trash icon-large pull-right")
        .click(function(event) {
            event.stopPropagation();
            deleteSelectedReminder();
        });
    
    var dateString = "";
    if (reminders[reminderIndex].date) {
        var dueDate = new Date(reminders[reminderIndex].date);
        dateString = dueDate.toLocaleDateString() + " " + dueDate.getHours() + ":" + (dueDate.getMinutes() === 0 ? "00" : dueDate.getMinutes());
    } else {
        dateString = "No date specified";
    }
     
    var link = $("<a>")
        .html(reminders[reminderIndex].title + "<br><span class='muted'>" + dateString + "</span>")
        .append(deleteIcon, mapIcon, editIcon);
    
    return $("<li>")
        .append(link)
        .click(function() {
            selectReminder(reminderIndex);
        });
}

function createSelectOptionForList(listIndex) {
    
    // Note that I chose to use the list's index as the value, not the list's id.
    var option = $("<option>").attr("value", listIndex).text(lists[listIndex].title);
    if (listIndex === selectedListIndex) {
        option.attr("selected", true);
    }
    return option;
}

/* Map */

function prepareMapDialog() {
    
    // Load the map with some default location (Ghent, Belgium).
    map = new google.maps.Map(document.getElementById("map"), {
        center: new google.maps.LatLng(51.05, 3.72),
        zoom: 14,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    });

    google.maps.event.addListener(map, "click", function(event) {
        setMarker(event.latLng);
    });
    
    $('#mapDialog').on("shown", function () {
        
        // This is needed to use a map in a Bootstrap modal dialog.
        google.maps.event.trigger(map, "resize");
        
        // Set the map to the stored location if there is any.
        // If not, use the current location.
        // If geolocation isn't available, use the default location.
        
        if (reminders[selectedReminderIndex].location) {
            var position = new google.maps.LatLng(reminders[selectedReminderIndex].location.latitude, reminders[selectedReminderIndex].location.longitude);
            map.setCenter(position);
            map.setZoom(14);
            setMarker(position);
        } else if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function(currentPosition) {
                var position = new google.maps.LatLng(currentPosition.coords.latitude, currentPosition.coords.longitude);
                map.setCenter(position);
                map.setZoom(14);
                setMarker(null);
            });
        } else {
            var position = new google.maps.LatLng(51.05, 3.72);
            map.setCenter(position);
            map.setZoom(14);
            setMarker(null);
        }
    });

    $("#mapDialogDelete").click(function() {
        setMarker(null);
    });
    
    $("#mapDialogCancel").click(function() {
        $("#mapDialog").modal("hide");
    });
    
    $("#mapDialogSave").click(function() {
        var reminder = jQuery.extend(true, {}, reminders[selectedReminderIndex]);
        if (marker) {
            reminder.location = {
                latitude: marker.getPosition().lat(),
                longitude: marker.getPosition().lng()
            };
        } else {
            reminder.location = null;
        }

        // Send the updated reminder to the back-end.
        var request = new XMLHttpRequest();
        request.open("PUT", BASE_URL + "/lists/" + reminder.list + "/reminders/" + reminder.id, true, USERNAME, PASSWORD);
        request.onload = function() {
            if (request.status === 204) {
                reminders.splice(selectedReminderIndex, 1, reminder);
                var oldElement = $("#reminderList li")[selectedReminderIndex];
                $(oldElement).replaceWith(createListElementForReminder(selectedReminderIndex));
                selectReminder(selectedReminderIndex);
                $("#mapDialog").modal("hide");
            } else {
                console.log("Error updating reminder: " + request.status + " " + request.responseText);
                $("#mapDialog").modal("hide");
            }
        };
        request.setRequestHeader("Content-Type", "application/json");
        request.send(JSON.stringify(reminder));
    });
};

/*
 * Moves the marker.
 * Call this function with null as an argument (or without any arguments) to clear the marker.
 */
function setMarker(position) {
    if (!position) {
        if (marker) {
            marker.setMap(null);
            marker = null;
        }
        $("#mapDialogDelete").hide();
    } else {
        if (!marker) {
            marker = new google.maps.Marker({
                map: map,
                position: position
            });
        } else {
            marker.setPosition(position);
        }
        $("#mapDialogDelete").show();
    }
}
