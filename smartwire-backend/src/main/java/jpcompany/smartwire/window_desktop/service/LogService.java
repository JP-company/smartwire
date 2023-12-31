package jpcompany.smartwire.window_desktop.service;

import jpcompany.smartwire.domain.Date;
import jpcompany.smartwire.domain.Log;
import jpcompany.smartwire.domain.Process;
import jpcompany.smartwire.window_desktop.dto.LogSaveDto;
import jpcompany.smartwire.window_desktop.repository.LogReceiverJdbcTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LogService {
    private final LogReceiverJdbcTemplateRepository logReceiverRepository;

    public void saveLog(LogSaveDto logSaveDto) {
        Integer machineId = logSaveDto.getMachineId();
        // 받은 로그에서 기계 id를 가지고 최근 날짜 id 추출
        Date recentDateDto = logReceiverRepository.findRecentDateByMachineId(machineId).orElse(null);

        Integer machineDateId;
        LocalDate recentDate;

        if (recentDateDto != null) {  // 최근날짜가 있으면
            machineDateId = recentDateDto.getId();
            recentDate = recentDateDto.getDate();
            // 로그에 날짜가 있고, DB의 최신날짜와 다르면 새로운 날짜 생성
            if (logSaveDto.getDate() != null && !logSaveDto.getDate().isEqual(recentDate)) {
                Date date = new Date();
                date.setDate(logSaveDto.getDate());
                date.setMachineId(machineId);
                log.info("날짜={}", date);

                Date date1 = logReceiverRepository.saveDate(date);
                machineDateId = date1.getId();
                recentDate = date1.getDate();
                log.info("기계 날짜 id={}", machineDateId);
            }
        } else {  // 최근날짜가 없으면
            recentDate = LocalDate.now();
            Date date = new Date();
            date.setDate(recentDate);
            date.setMachineId(machineId);
            log.info("날짜={}", date);
            Date newDate = logReceiverRepository.saveDate(date); // DB Date 테이블에 업로드
            machineDateId = newDate.getId();
        }

        // 기계 날짜 id를 가지고 최근 작업 id 추출
        log.info("machineDateId={}", machineDateId);
        Process process = logReceiverRepository.findRecentProcessByMachineId(machineId).orElse(null);
        Integer processId = null;
        // 작업이 없지 않고, 최근 작업이 끝난게 아니면 최근 작업으로 설정
        if (process != null && process.getFinishedTime() == null) {
            processId = process.getId();
        }

        // 로그가 작업 시작이면, 새로운 작업 생성, reset 이면 작업 종료
        if (logSaveDto.getLog().equals("start_작업 시작")) {
            Process processDto = new Process();
            processDto.setFile(logSaveDto.getFile());
            processDto.setThickness(logSaveDto.getThickness());
            processDto.setStartedTime(logSaveDto.getLogTime());
            processDto.setMachineDateId(machineDateId);
            processDto.setMachineId(machineId);
            processId = logReceiverRepository.saveProcess(processDto).getId();
        } else if (processId != null && logSaveDto.getLog().split("_")[0].equals("reset")) {
            logReceiverRepository.doneProcess(processId,
                    LocalDateTime.of(recentDate, logSaveDto.getLogTime()),
                    logSaveDto.getActualProcessTime()
            );
        }

        Log log = new Log();
        log.setLog(logSaveDto.getLog());
        log.setLogTime(logSaveDto.getLogTime());
        log.setProcessId(processId);
        log.setMachineDateId(machineDateId);
        log.setMachineId(machineId);
        logReceiverRepository.saveLog(log);
    }
}