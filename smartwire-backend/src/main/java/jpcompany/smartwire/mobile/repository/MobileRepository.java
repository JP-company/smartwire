package jpcompany.smartwire.mobile.repository;

import jpcompany.smartwire.mobile.dto.FCMTokenAndAlarmSettingDto;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class MobileRepository {

    private final NamedParameterJdbcTemplate template;
    private final SimpleJdbcInsert jdbcInsert;

    public MobileRepository(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("fcmtokens")
                .usingGeneratedKeyColumns("id")
                .usingColumns("fcm_token", "alarm_setting", "member_id");
    }

    public void saveFcmTokenAndAlarmSetting(FCMTokenAndAlarmSettingDto fcmTokenAndAlarmSettingDto) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(fcmTokenAndAlarmSettingDto);
        jdbcInsert.execute(param);
    }

    public void deleteFCMTokenById(Integer fcmTokensId) {
        String sql = "DELETE FROM fcmtokens WHERE id=:fcmTokensId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("fcmTokensId", fcmTokensId);
        template.update(sql, param);
    }

    public void deleteFcmTokenByFCMToken(FCMTokenAndAlarmSettingDto fcmTokenAndAlarmSettingDto) {
        String sql = "DELETE FROM fcmtokens WHERE fcm_token=:fcmToken";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("fcmToken", fcmTokenAndAlarmSettingDto.getFcmToken());
        template.update(sql, param);
    }

    public void updateAlarmSettingByFCMToken(FCMTokenAndAlarmSettingDto fcmTokenAndAlarmSettingDto) {
        String sql = "UPDATE fcmtokens SET alarm_setting =:alarmSetting WHERE fcm_token=:fcmToken";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("alarmSetting", fcmTokenAndAlarmSettingDto.getAlarmSetting())
                .addValue("fcmToken", fcmTokenAndAlarmSettingDto.getFcmToken());
        template.update(sql, param);
    }
}
