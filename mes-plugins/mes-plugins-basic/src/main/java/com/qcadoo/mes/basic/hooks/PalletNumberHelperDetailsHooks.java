/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic.hooks;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.PalletNumberGenerator;
import com.qcadoo.mes.basic.constants.PalletNumberHelperFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class PalletNumberHelperDetailsHooks {

    public static final String L_FORM = "form";

    public static final String L_WINDOW = "window";

    public static final String L_PRINT = "print";

    public static final String L_PRINT_PALLET_NUMBER_HELPER_REPORT = "printPalletNumberHelperReport";

    @Autowired
    private PalletNumberGenerator palletNumberGenerator;

    public void onBeforeRender(final ViewDefinitionState view) {
        fillPalletNumbers(view);

        disableFields(view);

        disableButtonsWhenNotSaved(view);
    }

    private void fillPalletNumbers(final ViewDefinitionState view) {
        FormComponent palletNumberHelperForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(PalletNumberHelperFields.QUANTITY);
        FieldComponent firstNumberField = (FieldComponent) view.getComponentByReference(PalletNumberHelperFields.FIRST_NUMBER);
        FieldComponent palletNumbersField = (FieldComponent) view
                .getComponentByReference(PalletNumberHelperFields.PALLET_NUMBERS);

        String quantity = (String) quantityField.getFieldValue();
        String firstNumber = (String) firstNumberField.getFieldValue();

        boolean isSaved = (palletNumberHelperForm.getEntityId() != null);

        if (isSaved && StringUtils.isNotEmpty(quantity) && StringUtils.isNotEmpty(firstNumber)) {
            List<String> palletNumbers = palletNumberGenerator.list(firstNumber, Integer.valueOf(quantity));

            palletNumbersField.setFieldValue(buildPalletNumbersString(palletNumbers));
            palletNumbersField.requestComponentUpdateState();
        }
    }

    private String buildPalletNumbersString(final List<String> palletNumbers) {
        StringBuilder palletNumbersString = new StringBuilder();

        int i = 0;

        for (String palletNumber : palletNumbers) {
            palletNumbersString.append(palletNumber);

            if (i != (palletNumbers.size() - 1)) {
                palletNumbersString.append("\n");
            }

            i++;
        }

        return palletNumbersString.toString();
    }

    private void disableFields(final ViewDefinitionState view) {
        FormComponent palletNumberHelperForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(PalletNumberHelperFields.QUANTITY);

        boolean isEnabled = (palletNumberHelperForm.getEntityId() == null);

        quantityField.setEnabled(isEnabled);
        quantityField.requestComponentUpdateState();
    }

    private void disableButtonsWhenNotSaved(final ViewDefinitionState view) {
        FormComponent palletNumberHelperForm = (FormComponent) view.getComponentByReference(L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup printRibbonGroup = ribbon.getGroupByName(L_PRINT);

        RibbonActionItem printPalletNumberReportHelperRibbonActionItem = printRibbonGroup
                .getItemByName(L_PRINT_PALLET_NUMBER_HELPER_REPORT);

        Long palletNumberHelperId = palletNumberHelperForm.getEntityId();

        boolean isEnabled = (palletNumberHelperId != null);

        if (printPalletNumberReportHelperRibbonActionItem != null) {
            printPalletNumberReportHelperRibbonActionItem.setEnabled(isEnabled);

            printPalletNumberReportHelperRibbonActionItem.requestUpdate(true);
        }
    }

}
