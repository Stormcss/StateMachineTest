package ru.strcss.test.cci;

/**
 * Created by Stormcss
 * Date: 24.11.2018
 */
public enum rqEvents {
    eReceive, //to 1
    eCheck, // 1 to 2
    eApprove, // 2 to 3
    eProccess, // 3 to 5
    eClose, // 5 to 7
    eUnapprove, // 2 to 4
    eCancel, // 4 to 6
    eIgnore, // 1 to 9
    eFinish
}
