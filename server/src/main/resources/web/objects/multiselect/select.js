function selectMultiInput() {

    function toOption(object, controller, loaded) {
        return {
            value: controller.getObjectsString(object),
            text: object.name,
            originalObject: object, // should be parsed by getChangeObjects
            loaded: loaded //for cache clearing
        }
    }

    function clearSearchCaches(selectize) {
        // Selectize stores the history of requests inside itself.
        // And if, for example, you enter "1" in the text field, click on this item, then delete this item, then enter "1" again - the "load" function will not be called.
        // Probably this is to avoid sending new requests for repeated characters with the expectation that the options will not be deleted, which, probably, does not work for us.
        // Clearing the request history can help
        selectize.loadedSearches = {};

        //clear the all unselected options to fully clear the cache
        for (let [key, value] of Object.entries(selectize.options)) {
            if (value.loaded)
                selectize.removeOption(key, true);
        }
    }

    return {
        render: function (element, controller) {
            let selectizeElement = _wrapElement(element, 'div', controller == null); // it seems that selectize uses parent (because it creates sibling) so for custom cell renderer we need extra div

            Selectize.define('on_key_down', function() {
                var self = this.onKeyDown;
                this.onKeyDown = function (e) {
                    //End editing by pressing shift+enter
                    if (e.shiftKey === true && e.key === 'Enter') {
                        this.close();
                        this.blur();
                    } else if (e.key === 'Escape') { //Esc pressed try to close form. Prevent it and only blur element
                        e.stopPropagation();
                        this.blur();
                    } else
                        self.apply(this, arguments);
                }
            });

            element.selectizeInstance = $(selectizeElement).selectize({
                dropdownParent: 'body',

                onItemAdd: function (value) {
                    if(!element.silent) {
                        let option = this.options[value];

                        // assert option.loaded;
                        option.loaded = false; // ??? or should be called updateOption

                        //onItemAdd is triggered after the item has been rendered and caret position has moved one ahead,
                        // for changeProperty we need the previous position of caret. for this reason -1
                        element.controller.changeProperty('selected', option.originalObject, true, "add", this.caretPos - 1);

                        //When option selected by mouseclick no possibility to continue the search from the keyboard
                        this.$control_input[0].focus();
                    }
                },
                onItemRemove: function (value) {
                    if(!element.silent) {
                        let originalObject = this.options[value].originalObject;

                        this.removeOption(value, true);

                        element.controller.changeProperty('selected', originalObject, null, "remove");
                    }
                },
                onDropdownOpen: function (dropdown) {
                    // setting autoHidePartner to avoid fake blurs
                    dropdown[0].autoHidePartner = element;
                    this.setCaret(this.items.length);
                },
                respect_word_boundaries: false, // undocumented feature. But for some reason it includes support for searching by Cyrillic characters
                preload: 'focus',
                loadThrottle: 0,
                load: function (query, callback) {
                    clearSearchCaches(this);

                    let controller = element.controller;
                    controller.getPropertyValues('name', query, (data) => {
                        let options = [];
                        for (let dataElement of data.data)
                            options.push(toOption(controller.createObject({selected : false, name : dataElement.rawString}, dataElement.objects), controller, true));
                        callback(options);
                    }, null);
                },
                plugins: ['remove_button', 'auto_position', 'on_key_down']
            });
        },
        update: function (element, controller, list) {
            element.silent = true; // onItemAdd / Remove somewhy is not silenced
            element.controller = controller;

            let isList = controller.isList();
            if (isList) {
                if (!controller.booleanFilterSet && list.length > 0) {
                    controller.setBooleanViewFilter('selected', 1000);
                    controller.booleanFilterSet = true;
                    return;
                }
            } else { // controller is needed in render() to add onItemAdd and onItemRemove listeners. In CustomCellRenderer we cannot pass the controller to render()
                if(list == null)
                    list = [];
            }

            let selectizeInstance = element.selectizeInstance[0].selectize;

            controller.diff(list, (type, index, object) => {
                let selectizeOption = toOption(object, controller, false);
                let optionValue = selectizeOption.value;

                switch(type) {
                    case 'remove':
                        selectizeInstance.removeOption(optionValue, true);
                        break;
                    case 'add':
                    case 'update':
                        //inside selectizeInstance.updateOption if option with value does not exist - do nothing.
                        //we need to update option to set loaded = false for clearSearchCaches
                        selectizeInstance.updateOption(optionValue, selectizeOption);
                        selectizeInstance.addOption(selectizeOption);

                        selectizeInstance.setCaret(index);
                        selectizeInstance.addItem(optionValue, true);
                        selectizeInstance.setCaret(selectizeInstance.items.length);
                }
            }, true, true);

            element.silent = false;
        },
        clear: function (element) {
            element.selectizeInstance[0].selectize.destroy();
        }
    }
}

function selectList() {
    return _defaultRadioCheckBox('radio', true, true);
}

function selectButton() {
    return _checkBoxRadioButtonToggle('radio', true, true);
}

function selectButtonGroup() {
    return _checkBoxRadioButtonGroup('radio', true, true);
}

function selectMultiList() {
    return _defaultRadioCheckBox('checkbox', false, false, true);
}

function selectMultiButton() {
    return _checkBoxRadioButtonToggle('checkbox', false, false, true);
}

function selectMultiButtonGroup() {
    return _checkBoxRadioButtonGroup('checkbox', false, false, true);
}

function selectNullList() {
    return _defaultRadioCheckBox('radio', false, true);
}

function selectNullButton() {
    return _checkBoxRadioButtonToggle('radio', false, true);
}

function selectNullButtonGroup() {
    return _checkBoxRadioButtonGroup('radio', false, true);
}

function _defaultRadioCheckBox(type, shouldBeSelected, hasName, multi) {
    return _option(type, false, ['form-check'], ['form-check-input'], ['form-check-label', 'option-item'], shouldBeSelected, hasName, multi);
}

function _checkBoxRadioButtonToggle(type, shouldBeSelected, hasName, multi) {
    return _option(type, false, null, ['btn-check'], ['btn', 'btn-outline-secondary', 'option-item'], shouldBeSelected, hasName, multi);
}

function _checkBoxRadioButtonGroup(type, shouldBeSelected, hasName, multi) {
    return _option(type, true, ['btn-group'], ['btn-check'], ['btn', 'btn-outline-secondary', 'option-item'], shouldBeSelected, hasName, multi);
}

function _wrapElement(element, tag, wrap) {
    element.classList.add("comp-shrinked");

    let wrapElement = element;
    if(wrap) {
        wrapElement = document.createElement(tag);
        wrapElement.classList.add("fill-parent-perc")
        element.appendChild(wrapElement);
    }
    return wrapElement;
}

function _option(type, isGroup, divClasses, inputClasses, labelClasses, shouldBeSelected, hasName, multi) {
    let isButton = isGroup || divClasses == null;

    function _getRandomId() {
        return Math.random().toString(36).slice(2);
    }

    /* if isInnerElement==true this is <label> or <input> element.
    * if isInnerElement==false this is <div> element*/
    function _getOptionElement(options, index, isInput, isInnerElement) {
        if (isButton) {
            return options.children[(index * 2) + (isInput ? 0 : 1)]
        } else {
            let option = options.children[index];
            return isInnerElement ? isInput ? option.firstChild : option.lastChild : option;
        }
    }

    return {
        render: function (element) {
            let options = _wrapElement(element, 'div', false);

            element.options = options;

            element.name = hasName ? _getRandomId() : null; // radiobutton must have a name attribute

            options.classList.add(isButton ? "option-btn-container" : "option-container");
            if (isGroup) {
                options.setAttribute("role", "group");
                if (divClasses != null)
                    divClasses.forEach(divClass => options.classList.add(divClass));
            }
        }, update: function (element, controller, list) {
            let isList = controller.isList();
            list = _convertList(isList, list);
            // need this dropAndNotSetChecked to properly handle situations when there are 2 selected in non-multi select
            let changed = { dropAndNotSetChecked : false}
            let options = element.options;
            controller.diff(list, (changeType, index, object) => {
                switch(changeType) {
                    case 'remove': // clear
                        if (isButton) {
                            options.removeChild(_getOptionElement(options, index, false, false));
                            options.removeChild(_getOptionElement(options, index, true, false));
                        } else {
                            options.removeChild(_getOptionElement(options, index, false, false));
                        }
                        break;
                    case 'add': // render and update
                    case 'update': // update
                        let input, label;
                        if(changeType === 'add') {
                            input = document.createElement('input');
                            inputClasses.forEach(inputClass => input.classList.add(inputClass));
                            input.type = type;
                            input.id = _getRandomId();
                            input.setAttribute("autocomplete", 'off');

                            input.setAttribute('name', element.name);

                            label = document.createElement('label');
                            labelClasses.forEach(labelClass => label.classList.add(labelClass));
                            label.setAttribute('for', input.id);

                            input.addEventListener('click', function () {
                                let object = this.object;
                                let set;
                                if (!multi && !shouldBeSelected && element.prevSelected === object)
                                    set = false;
                                else
                                    set = this.checked;

                                if(!multi) {
                                    element.prevSelected = set ? object : null;
                                    if(!set) // radio button won't uncheck itself
                                        this.checked = false;
                                }
                                controller.changeProperty('selected', object, set ? true : null);
                            });

                            let currentOptions = options.children;
                            let append = index === (isButton ? currentOptions.length / 2 : currentOptions.length);
                            if (isButton) {
                                if (append) {
                                    options.appendChild(input);
                                    options.appendChild(label);
                                } else {
                                    options.insertBefore(input, currentOptions[index * 2]);
                                    options.insertBefore(label, currentOptions[(index * 2) + 1]);
                                }
                            } else {
                                let div = document.createElement('div');
                                divClasses.forEach(divClass => div.classList.add(divClass));
                                div.appendChild(input);
                                div.appendChild(label);

                                if (append)
                                    options.appendChild(div);
                                else
                                    options.insertBefore(div, currentOptions[index]);
                            }
                        } else {
                            input = _getOptionElement(options, index, true, true);
                            label = _getOptionElement(options, index, false, true);
                        }

                        input.object = object;
                        label.innerText = object.name;
                        // we can't use required for styling, because we want "live validation" and not on submit
                        // input.required = shouldBeSelected;

                        let checked = object.selected;
                        if(!multi) {
                            if (checked) {
                                changed.dropAndNotSetChecked = false;
                                element.prevSelected = object;
                            } else if (input.checked)
                                changed.dropAndNotSetChecked = true;
                        }
                        input.checked = checked;
                        break;
                }
            });

            // if we dropped (and not set) checked and there are other selected elements - select them
            let hasSelected = false;
            if (!multi && (changed.dropAndNotSetChecked || shouldBeSelected)) {
                for (let i = 0; i < list.length; i++) {
                    let object = list[i];
                    if (object.selected != null && object.selected) {
                        if(changed.dropAndNotSetChecked) {
                            let input = _getOptionElement(options, i, true, true);
                            input.checked = true;
                            element.prevSelected = object;
                        }
                        hasSelected = true;
                        break;
                    }
                }
                if(changed.dropAndNotSetChecked && !hasSelected)
                    element.prevSelected = null;
            }

            for (let i = 0; i < list.length; i++) {
                let input = _getOptionElement(options, i, true, true);
                if (isList) {
                    if (controller.isCurrent(input.object))
                        input.classList.add('option-item-current');
                    else
                        input.classList.remove('option-item-current');

                    _setReadonly(input, controller.isPropertyReadOnly('selected', input.object));
                } else {
                    _setReadonly(input, controller.isReadOnly());
                }

                if (!multi && shouldBeSelected) {
                    if (isButton) { // bootstrap doesn't support is-invalid for buttons
                        let label = _getOptionElement(options, i, false, true);
                        if (!hasSelected) {
                            label.classList.add("btn-outline-danger");
                            label.classList.remove("btn-outline-secondary");
                        } else {
                            label.classList.add("btn-outline-secondary");
                            label.classList.remove("btn-outline-danger");
                        }
                    } else {
                        if (!hasSelected)
                            input.classList.add("is-invalid");
                        else
                            input.classList.remove("is-invalid");
                    }
                }
            }
        }
    }
}

// todo --- backward compatibility. option() should be removed in future releases ---
function option() {
    return selectButton();
}

function selectNullDropdown() {
    return _selectDropdown(false);
}

function selectDropdown() {
    return _selectDropdown(true);
}

function selectMultiDropdown() {
    return _dropDown(['multi-select'],
        {'data-container': 'body', 'multiple': ''},
        (element) => {
            let selectElement = $(element.select);
            selectElement.selectpicker();
            selectElement.on('changed.bs.select', function (e, clickedIndex, isSelected) {
            if (clickedIndex != null && isSelected != null) //documentation:  If the select's value has been changed either via the .selectpicker('val'), .selectpicker('selectAll'), or .selectpicker('deselectAll') methods, clickedIndex and isSelected will be null.
                element.controller.changeProperty('selected', this.children[clickedIndex].object, isSelected ? true : null);
        })},
        true, false);
}

function _selectDropdown(shouldBeSelected) {
    return _dropDown(['form-select'],
        {},
        (element) => {
            element.select.addEventListener('change', function () {
                let object = this.selectedOptions[0].object;
                let set;
                if(!object) {
                    object = element.prevSelected;
                    if(!object) { /* was null and remained null */
                        // assert false;
                        return;
                    }

                    set = false;
                } else
                    set = true;

                element.prevSelected = set ? object : null;
                element.controller.changeProperty('selected', object, set ? true : null);
            })
        },
        false, shouldBeSelected);
}

function _dropDown(cssClasses, selectAttributes, eventListener, multi, shouldBeSelected) {
    return {
        render: function (element, controller) {
            let select = _wrapElement(element, 'select', element.tagName.toLowerCase() !== 'select');

            element.select = select;

            cssClasses.forEach(cssClass => select.classList.add(cssClass));

            Object.keys(selectAttributes).forEach(key => select.setAttribute(key, selectAttributes[key]));

            // Because there is no default way to reset the value of the drop-down list to null, we must create a null-option
            // if there are no selected options, the selected option becomes a null-option.
            // It is also necessary to make offset when searching by index
            if (!multi) {
                let option = document.createElement('option');
                option.hidden = shouldBeSelected;
                select.appendChild(option);
            }

            eventListener(element);
        },
        update: function (element, controller, list) {
            element.controller = controller;
            let isList = controller.isList();
            list = _convertList(isList, list);
            let select = element.select;
            // need this dropAndNotSetChecked to properly handle situations when there are 2 selected in non-multi select
            let changed = { dropAndNotSetChecked : false}
            let offset = multi ? 0 : 1;
            controller.diff(list, (changeType, index, object) => {
                switch(changeType) {
                    case 'remove':
                        select.removeChild(select.children[index + offset]);
                        break;
                    case 'add':
                    case 'update':
                        let option;
                        if(changeType === 'add') {
                            option = document.createElement('option');
                            let currentOptions = select.children;
                            if (index === currentOptions.length + offset)
                                select.appendChild(option);
                            else
                                select.insertBefore(option, currentOptions[index + offset]);

                        } else {
                            option = select.children[index + offset];
                        }

                        option.object = object;
                        option.innerText = object.name;

                        let checked = object.selected;
                        if(!multi) {
                            if (checked) {
                                changed.dropAndNotSetChecked = false;
                                element.prevSelected = object;
                            } else if (option.selected)
                                changed.dropAndNotSetChecked = true;
                        }
                        option.selected = checked;

                        break;
                }
            });

            if (!multi) {
                let hasSelected = false;
                for (let i = 0; i < list.length; i++) {
                    let object = list[i];
                    if (object.selected) {
                        if(changed.dropAndNotSetChecked) {
                            select.options[i + offset].selected = true;
                            element.prevSelected = object;
                        }
                        hasSelected = true;
                        break;
                    }
                }
                if(changed.dropAndNotSetChecked && !hasSelected) {
                    select.options[0].selected = true; //select the null option
                    element.prevSelected = null;
                }

                if (shouldBeSelected) {
                    if (hasSelected)
                        select.classList.remove("is-invalid");
                    else
                        select.classList.add("is-invalid");
                }
            }

            if (isList) {
                for (let i = 0; i < list.length; i++) {
                    let option = select.options[i + offset];
                    option.setAttribute("readonly", "");
                    _setReadonly(option, controller.isPropertyReadOnly('selected', option.object));
                }
            } else {
                _setReadonly(select, controller.isReadOnly());
            }

            if (multi)
                $(select).selectpicker('refresh');
        },
        clear: function (element) {

        }
    }
}

function _setReadonly(element, readonly) {
    if(readonly)
        element.setAttribute('onclick', 'return false');
    else
        element.removeAttribute('onclick')
}

function _convertList(isList, list) {
    if(!isList) {
        if (typeof list === 'string') {
            let strings = list.split(",");
            list = [];
            for (let i = 0; i < strings.length; i++) {
                list.push({name: strings[i], selected: false});
            }
        } else if (list == null) {
            list = [];
        }
    }
    return list;
}
