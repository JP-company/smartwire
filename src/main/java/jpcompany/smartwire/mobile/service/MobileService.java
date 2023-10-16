package jpcompany.smartwire.mobile.service;

import jpcompany.smartwire.mobile.dto.FCMTokenAndAlarmSettingDto;
import jpcompany.smartwire.mobile.repository.MobileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MobileService {

    private final MobileRepository mobileRepository;

    public void saveFcmTokenAndAlarmSettingFromDB(FCMTokenAndAlarmSettingDto fcmTokenAndAlarmSettingDto) {
        mobileRepository.saveFcmTokenAndAlarmSetting(fcmTokenAndAlarmSettingDto);
    }

    public void deleteFcmTokenAndAlarmSettingFromDB(FCMTokenAndAlarmSettingDto fcmTokenAndAlarmSettingDto) {
        mobileRepository.deleteFcmTokenByFCMToken(fcmTokenAndAlarmSettingDto);
    }
}
